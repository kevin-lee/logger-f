package loggerf.logger.logback

import cats.effect._
import cats.effect.unsafe.IORuntime
import cats.syntax.all._
import ch.qos.logback.classic.{Level, LoggerContext}
import hedgehog._
import hedgehog.runner._
import org.slf4j.{LoggerFactory, MDC}

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Callable, ExecutorService, Executors, TimeUnit}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** Verifies the per-thread fallback of Ce3MdcAdapter: on threads with no running fiber,
  * MDC behaves like the stock LogbackMDCAdapter (Monix-Local-like graceful degradation
  * for fiber-unaware code), strictly isolated from the fiber context in both directions.
  *
  * @author Kevin Lee
  * @since 2026-07-04
  */
object Ce3MdcAdapterFallbackSpec extends Properties {

  /* Real runtime: the isolation tests need genuine root fibers (unsafeRunSync) running
   * next to plain-thread code. The hedgehog test thread itself never runs a fiber,
   * which is exactly what makes it a "non-fiber thread" in these tests. */
  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global

  /* Install the adapter globally once per suite (the suites of this module run serially,
   * see Test / parallelExecution := false in build.sbt). The instance is kept because
   * getKeys is not reachable through the org.slf4j.MDC facade. */
  private val ce3MdcAdapter: Ce3MdcAdapter = Ce3MdcAdapter.initialize()

  override def tests: List[Test] = List(
    property(
      "non-fiber thread - MDC put/get/remove/clear should work like classic ThreadLocal MDC",
      testNonFiberBasics,
    ),
    property(
      "non-fiber thread - getCopyOfContextMap/getKeys/setContextMap should reflect the thread's values",
      testNonFiberContextMap,
    ),
    property("non-fiber thread - logback event should capture the thread's MDC", testNonFiberLogging),
    property(
      "isolation - values put on a plain thread should not be visible inside a fiber and should survive the fiber run",
      testThreadToFiberIsolation,
    ),
    property(
      "isolation - values put inside a fiber should not be visible on the launching thread",
      testFiberToThreadIsolation,
    ),
    property(
      "alternating thread (evalOn) - fiber and non-fiber MDC on the same thread should stay independent",
      testAlternatingThreadIndependence,
    ),
    property("concurrent plain threads should each keep their own MDC values", testConcurrentPlainThreads),
    property(
      "bridging - MDC captured on a plain thread and seeded inside a fiber should be inherited by child fibers",
      testThreadMdcBridging,
    ),
    property(
      "stress - concurrent fibers and plain threads on the same key should never cross-contaminate",
      testConcurrentFibersAndPlainThreadsStress,
    ),
  )

  /* MDC.clear() on the (non-fiber) test thread clears that thread's fallback map.
   * This is real hygiene here, not a formality: hedgehog reuses its test thread across
   * property evaluations, so leftover fallback values would leak between evaluations. */
  def before(): Unit = MDC.clear()

  /* Without the fallback, every call below would be a silent no-op (put/remove/clear)
   * or return null (get): CE's IOLocal#unsafeThreadLocal only works while a fiber is
   * running, and this test runs on the plain hedgehog thread. The fallback must restore
   * the stock LogbackMDCAdapter (and Monix Local) behaviour for exactly this kind of
   * fiber-unaware caller — servlet filters, tracing libraries, or any code that logs
   * on its own threads. */
  def testNonFiberBasics: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
      newValue     <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("new:" + _).log("newValue")
    } yield {
      before()

      val key = keyValuePair.key

      val beforePut = (Option(MDC.get(key)) ==== none).log("before put")

      MDC.put(key, keyValuePair.value)
      val afterPut = (MDC.get(key) ==== keyValuePair.value).log("after put")

      MDC.put(key, newValue)
      val afterOverwrite = (MDC.get(key) ==== newValue).log("after overwrite")

      MDC.remove(key)
      val afterRemove = (Option(MDC.get(key)) ==== none).log("after remove")

      MDC.put(key, keyValuePair.value)
      MDC.clear()
      val afterClear = (Option(MDC.get(key)) ==== none).log("after clear")

      Result.all(
        List(
          beforePut,
          afterPut,
          afterOverwrite,
          afterRemove,
          afterClear,
        )
      )
    }

  /* Same as above, but for the bulk operations logback itself relies on:
   * getCopyOfContextMap is what logback reads when capturing an event's MDC (e.g. for
   * async appenders), and setContextMap is how frameworks restore a captured context
   * onto a worker. Both must target the thread's fallback map when no fiber is running. */
  def testNonFiberContextMap: Property =
    for {
      keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
      replacement   <- Gens.genKeyValuePair.map(kv => kv.copy(key = "replacement:" + kv.key)).log("replacement")
    } yield {
      before()

      val expected = keyValuePairs.keyValuePairs.map(kv => kv.key -> kv.value).toMap
      keyValuePairs.keyValuePairs.foreach(keyValue => MDC.put(keyValue.key, keyValue.value))

      val copyOfContextMap = (MDC.getCopyOfContextMap.asScala.toMap ==== expected).log("getCopyOfContextMap")

      /* getKeys is only on the adapter itself; the org.slf4j.MDC facade doesn't expose it. */
      val keySet = (ce3MdcAdapter.getKeys.asScala.toSet ==== expected.keySet).log("getKeys")

      MDC.setContextMap(Map(replacement.key -> replacement.value).asJava)
      val afterSetContextMap       =
        (MDC.getCopyOfContextMap.asScala.toMap ==== Map(replacement.key -> replacement.value))
          .log("after setContextMap")
      val keySetAfterSetContextMap =
        (ce3MdcAdapter.getKeys.asScala.toSet ==== Set(replacement.key)).log("getKeys after setContextMap")

      Result.all(
        List(
          copyOfContextMap,
          keySet,
          afterSetContextMap,
          keySetAfterSetContextMap,
        )
      )
    }

  /* End-to-end on a plain thread — the servlet-filter pattern: fiber-unaware code does
   * MDC.put and then logs synchronously on the same thread. The logback event must carry
   * the value; logback reads the adapter's getPropertyMap during append, which must
   * resolve to the fallback map here. Before the fallback existed, this event's MDC map
   * was silently empty. */
  def testNonFiberLogging: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      before()

      val loggerContext = getLoggerContext
      val appender      = new MdcCapturingAppender
      appender.setContext(loggerContext)
      appender.start()

      /* Fresh logger name per property evaluation: logback caches loggers by name, so
       * reusing one name would pile appenders up and mix events between evaluations. */
      val logger = loggerContext.getLogger(s"mdc-fallback-spec-logger-${loggerNameCounter.incrementAndGet()}")
      logger.addAppender(appender)
      logger.setLevel(Level.INFO)
      logger.setAdditive(false)

      try {
        MDC.put(keyValuePair.key, keyValuePair.value)
        logger.info("fallback-message")

        val mdcByMessage = appender.capturedEvents.toMap
        (mdcByMessage.get("fallback-message").flatMap(_.get(keyValuePair.key)) ==== keyValuePair.value.some)
          .log("the logback event logged on a plain thread did not capture the thread's MDC")
      } finally {
        /* Detach even on failure so later evaluations never log into this appender. */
        val _ = logger.detachAppender(appender)
        appender.stop()
      }
    }

  /* Fallback values must NOT flow into fibers. Two reasons combine here: a CE root fiber
   * starts with an empty IOLocal state (it inherits only from a parent fiber, and this
   * plain launching thread has none), and the adapter deliberately never falls through
   * to the fallback map while a fiber is running — merging the two worlds would leak
   * unrelated thread state into fibers. The thread's own value must also survive the
   * fiber run untouched. (This is the documented difference from Monix, whose shared
   * Local context DOES flow through runSyncUnsafe into the task.) */
  def testThreadToFiberIsolation: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      before()

      MDC.put(keyValuePair.key, keyValuePair.value)

      val inFiber  = IO(Option(MDC.get(keyValuePair.key))).unsafeRunSync()
      val afterRun = MDC.get(keyValuePair.key)

      Result.all(
        List(
          (inFiber ==== none).log("the plain thread's value leaked into the fiber"),
          (afterRun ==== keyValuePair.value).log("the plain thread's value was lost after the fiber run"),
        )
      )
    }

  /* The reverse direction: writes inside a fiber live in the fiber's IOLocal state and
   * die with the fiber; they must never end up in the launching thread's fallback map. */
  def testFiberToThreadIsolation: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      before()

      IO(MDC.put(keyValuePair.key, keyValuePair.value)).unsafeRunSync()

      (Option(MDC.get(keyValuePair.key)) ==== none).log("the fiber's value leaked to the launching thread")
    }

  /* THE critical invariant behind "never merge": one physical thread can host BOTH
   * worlds over time. The real-life example is a Netty event-loop thread — library code
   * writes fallback MDC on it directly, while evalOn can also run fiber segments on it.
   * Whichever mode is active must see only its own storage:
   *   1. seed the fallback map by running plain Runnables on a dedicated thread,
   *   2. run a fiber segment on that SAME thread via evalOn — it must not see the
   *      fallback values, and its own put must land in the fiber context,
   *   3. run plain code on the thread again — its fallback values must be intact and
   *      must not contain the fiber's key. */
  def testAlternatingThreadIndependence: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      val threadKey   = "thread:" + keyValuePair.key
      val threadValue = "thread:" + keyValuePair.value
      val fiberKey    = "fiber:" + keyValuePair.key
      val fiberValue  = "fiber:" + keyValuePair.value

      val executor = Executors.newSingleThreadExecutor { (runnable: Runnable) =>
        val thread = new Thread(runnable, "mdc-fallback-spec-alternating")
        thread.setDaemon(true)
        thread
      }
      try {
        val ec = ExecutionContext.fromExecutor(executor)

        /* Seed the fallback MDC by running plain (non-fiber) code on the dedicated thread. */
        runOn(executor)(MDC.put(threadKey, threadValue))
        val seeded = runOn(executor)(Option(MDC.get(threadKey)))

        /* Run a fiber segment on the same thread via evalOn: the fiber-presence probe
         * must pick the fiber context here even though this very thread's fallback map
         * is non-empty at this moment. */
        val (fiberSawThreadValue, fiberOwnValue) =
          IO {
            val saw = Option(MDC.get(threadKey))
            MDC.put(fiberKey, fiberValue)
            (saw, Option(MDC.get(fiberKey)))
          }.evalOn(ec)
            .unsafeRunSync()

        /* Back to plain code on the same thread. */
        val threadValueAfter   = runOn(executor)(Option(MDC.get(threadKey)))
        val fiberValueOnThread = runOn(executor)(Option(MDC.get(fiberKey)))

        Result.all(
          List(
            (seeded ==== threadValue.some).log("seeding the fallback MDC on the dedicated thread failed"),
            (fiberSawThreadValue ==== none).log("the fiber saw the thread's fallback value"),
            (fiberOwnValue ==== fiberValue.some).log("the fiber's own put was not visible inside the fiber"),
            (threadValueAfter ==== threadValue.some)
              .log("the thread's fallback value was damaged by the fiber segment"),
            (fiberValueOnThread ==== none).log("the fiber's value leaked into the thread's fallback"),
          )
        )
      } finally {
        val _ = executor.shutdown()
      }
    }

  /* The fallback is a plain ThreadLocal, so concurrent plain threads writing the SAME
   * key must never observe each other's values — the classic per-thread MDC contract
   * that thread-per-request code relies on. */
  def testConcurrentPlainThreads: Property =
    for {
      keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
    } yield {
      before()

      val sharedKey = "shared-key"
      val results   = new java.util.concurrent.ConcurrentHashMap[String, Option[String]]()

      val threads = keyValuePairs.keyValuePairs.map { keyValue =>
        new Thread(() => {
          MDC.put(sharedKey, keyValue.value)
          results.put(keyValue.value, Option(MDC.get(sharedKey)))
          MDC.clear()
        })
      }
      threads.foreach(_.start())
      threads.foreach(_.join(5000L))

      Result.all(
        keyValuePairs.keyValuePairs.map { keyValue =>
          (Option(results.get(keyValue.value)).flatten ==== keyValue.value.some)
            .log(s"the plain thread that put ${keyValue.value} did not see its own value")
        }
      )
    }

  /* The documented bridge for the one flow CE3 does NOT provide automatically (but Monix
   * did): getting MDC set on a plain thread INTO fibers. A CE root fiber always starts
   * with an empty IOLocal state, so the context has to be carried across the unsafe
   * boundary by hand:
   *   capture on the plain thread -> seed inside the root fiber as its first step ->
   *   every fiber forked afterwards inherits the seeded context via copy-on-fork.
   * The capture step only works because of the fallback: without it,
   * MDC.getCopyOfContextMap on a plain thread always returned an empty map. */
  def testThreadMdcBridging: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      before()

      /* 1. Fiber-unaware code (e.g. a servlet filter) sets MDC on the plain thread. */
      MDC.put(keyValuePair.key, keyValuePair.value)

      /* 2. Capture on the plain thread, right before entering the effect world. */
      val captured = MDC.getCopyOfContextMap

      /* 3. Seed the captured context as the first step inside the root fiber. */
      val (inRootFiber, inChildFiber, afterBoundary) =
        (for {
          _             <- IO(MDC.setContextMap(captured))
          inRootFiber   <- IO(Option(MDC.get(keyValuePair.key)))
          inChildFiber  <- IO(Option(MDC.get(keyValuePair.key))).start.flatMap(_.joinWithNever)
          _             <- IO.sleep(1.millis)
          afterBoundary <- IO(Option(MDC.get(keyValuePair.key)))
        } yield (inRootFiber, inChildFiber, afterBoundary)).unsafeRunSync()

      val threadValueAfter = MDC.get(keyValuePair.key)

      Result.all(
        List(
          (inRootFiber ==== keyValuePair.value.some).log("the seeded context was not visible in the root fiber"),
          (inChildFiber ==== keyValuePair.value.some)
            .log("the seeded context was not inherited by a forked child fiber"),
          (afterBoundary ==== keyValuePair.value.some).log("the seeded context was lost after an async boundary"),
          (threadValueAfter ==== keyValuePair.value)
            .log("the plain thread's own MDC was damaged by seeding it into a fiber"),
        )
      )
    }

  /* Both worlds hammering the SAME key at the same time: fibers interleave on the
   * compute pool while raw threads run in parallel, all going through the same adapter
   * (and therefore the same fiber-presence probe). Every participant must only ever
   * read back its own value — any cross-talk between the fiber contexts and the
   * per-thread fallback maps, or between threads, fails this property.
   * Thread.sleep is deliberate here: it keeps the raw threads alive and reading while
   * the fibers interleave, which is the whole point of the stress. */
  @SuppressWarnings(Array("org.wartremover.warts.ThreadSleep"))
  def testConcurrentFibersAndPlainThreadsStress: Property =
    for {
      keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
    } yield {
      before()

      val sharedKey = "shared-key"
      val rounds    = 5

      /* Raw threads: put once, then repeatedly read while everything else runs. */
      val threadSeen = new java.util.concurrent.ConcurrentHashMap[String, List[Option[String]]]()
      val threads    = keyValuePairs.keyValuePairs.map { keyValue =>
        new Thread(() => {
          val ownValue = "thread:" + keyValue.value
          MDC.put(sharedKey, ownValue)
          val seen     = List.fill(rounds) {
            Thread.sleep(1L)
            Option(MDC.get(sharedKey))
          }
          threadSeen.put(keyValue.value, seen)
          MDC.clear()
        })
      }
      threads.foreach(_.start())

      /* Fibers: same key, with async boundaries between reads to force interleaving on
       * the worker threads while the raw threads above are running. */
      val fiberSeen =
        keyValuePairs
          .keyValuePairs
          .parTraverse { keyValue =>
            val ownValue = "fiber:" + keyValue.value
            IO(MDC.put(sharedKey, ownValue)) *>
              (IO.cede *> IO.sleep(1.millis) *> IO(Option(MDC.get(sharedKey))))
                .replicateA(rounds)
                .map(keyValue.value -> _)
          }
          .unsafeRunSync()

      threads.foreach(_.join(5000L))

      Result.all(
        fiberSeen.map {
          case (value, seen) =>
            (seen ==== List.fill(rounds)(("fiber:" + value).some))
              .log(s"the fiber that put fiber:$value saw someone else's value: $seen")
        } ++
          keyValuePairs.keyValuePairs.map { keyValue =>
            (Option(threadSeen.get(keyValue.value)).getOrElse(List.empty) ====
              List.fill(rounds)(("thread:" + keyValue.value).some))
              .log(s"the plain thread that put thread:${keyValue.value} saw someone else's value")
          }
      )
    }

  private val loggerNameCounter = new AtomicInteger(0)

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf"))
  private def getLoggerContext: LoggerContext =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  /** Runs the body as plain (non-fiber) code on the given executor and waits for the
    * result. Unlike evalOn, work submitted this way runs with no fiber registered on the
    * thread, so MDC calls inside it resolve to the fallback map. An explicit Callable
    * (not a lambda) is used because ExecutorService#submit is overloaded on
    * Runnable/Callable and lambda overload resolution differs across Scala versions.
    */
  private def runOn[A](executor: ExecutorService)(body: => A): A =
    executor
      .submit(new Callable[A] {
        override def call(): A = body
      })
      .get(5L, TimeUnit.SECONDS)

}
