package loggerf.logger.logback

import cats.effect._
import cats.effect.unsafe.IORuntime
import cats.syntax.all._
import ch.qos.logback.classic.{Level, LoggerContext}
import hedgehog._
import hedgehog.runner._
import org.slf4j.{LoggerFactory, MDC}

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/** Verifies the actual point of fiber-local MDC (from “Better logging with Monix 3, part 1: MDC”)
  * for Cats Effect 3: values put inside a fiber must survive real async boundaries
  * (sleep, cede, evalOn, blocking), stay isolated between concurrent fibers,
  * and be captured by real logback events.
  *
  * These tests deliberately use the real global IORuntime with unsafeRunSync
  * (not the mock-time single-threaded ticker of extras-hedgehog-ce3's runIO),
  * so fibers genuinely hop threads.
  *
  * @author Kevin Lee
  * @since 2026-07-04
  */
object Ce3MdcAdapterPropagationSpec extends Properties {

  /* The real work-stealing runtime: fibers are scheduled on the io-compute pool and can
   * resume on a different thread after an async boundary, which is exactly what these
   * tests need to prove. extras-hedgehog-ce3's runIO would run everything on one thread
   * with a mock clock, so it cannot prove propagation across threads. */
  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global

  /* Install the adapter as the global slf4j/logback MDC backend once per suite, before
   * any test touches MDC. Each suite installs its own instance, which is safe because
   * the suites of this module run serially (Test / parallelExecution := false). */
  {
    val _ = Ce3MdcAdapter.initialize()
  }

  override def tests: List[Test] = List(
    property("IO - MDC value should survive a real async boundary (IO.sleep)", testPropagationAcrossSleep),
    property("IO - MDC value should survive IO.cede", testPropagationAcrossCede),
    property("IO - MDC value should survive evalOn to another thread pool and back", testPropagationAcrossEvalOn),
    property("IO - MDC value should survive IO.blocking and back", testPropagationAcrossBlocking),
    property(
      "IO - concurrent fibers using the same key should keep isolated values across async boundaries",
      testConcurrentFibersIsolation,
    ),
    property("IO - logback event should capture the MDC of the logging fiber", testLogbackEventCapturesFiberMdc),
  )

  /* The core claim of the MDC blog post: a value put before an async boundary is still
   * there after it. IO.sleep is a real boundary — the fiber deschedules, a timer fires,
   * and the runtime may resume the fiber on a DIFFERENT worker thread. A plain
   * ThreadLocal-based MDC adapter loses the value whenever the thread changes;
   * the IOLocal-based one must not, because the state travels with the fiber. */
  def testPropagationAcrossSleep: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      (for {
        _   <- IO(MDC.put(keyValuePair.key, keyValuePair.value)) // put inside the fiber, before the boundary
        _   <- IO.sleep(5.millis) // real async boundary: deschedule + timer + reschedule
        got <- IO(MDC.get(keyValuePair.key)) // read after the boundary, possibly on another thread
      } yield (got ==== keyValuePair.value).log("MDC value was lost after IO.sleep"))
        .unsafeRunSync()
    }

  /* Same claim for explicit rescheduling. This boundary matters for the test suite
   * itself: a chain of IO(...) steps normally runs as ONE uninterrupted run-loop segment
   * on one thread (the runtime only auto-cedes every ~1024 stages), so a put-then-get
   * without a boundary would pass even with a plain ThreadLocal adapter. IO.cede forces
   * the fiber back into the scheduler; repeating it makes a thread switch likely. */
  def testPropagationAcrossCede: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      (for {
        _   <- IO(MDC.put(keyValuePair.key, keyValuePair.value))
        _   <- IO.cede.replicateA_(10) // 10 explicit reschedule points
        got <- IO(MDC.get(keyValuePair.key))
      } yield (got ==== keyValuePair.value).log("MDC value was lost after IO.cede"))
        .unsafeRunSync()
    }

  /* evalOn moves the SAME fiber onto a caller-provided thread pool and back, e.g. onto a
   * Netty or database pool. The MDC must follow the fiber in both directions: a value
   * put on the compute pool must be readable inside evalOn, and a value put inside
   * evalOn must still be there after returning. A dedicated single-thread executor with
   * a fixed thread name proves the thread really changed (asserted below), so this test
   * cannot silently pass by staying on one thread. */
  def testPropagationAcrossEvalOn: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      val key2   = keyValuePair.key + "-inside-evalOn"
      val value2 = keyValuePair.value + "-inside-evalOn"

      val evalOnThreadName = "mdc-propagation-spec-evalon"
      val acquireExecutor  = IO(
        Executors.newSingleThreadExecutor { (runnable: Runnable) =>
          val thread = new Thread(runnable, evalOnThreadName)
          thread.setDaemon(true)
          thread
        }
      )

      /* bracket guarantees the executor is shut down even when an assertion fails. */
      acquireExecutor
        .bracket { executor =>
          val ec = ExecutionContext.fromExecutor(executor)
          for {
            _             <- IO(MDC.put(keyValuePair.key, keyValuePair.value))
            computeThread <- IO(Thread.currentThread().getName)
            inside        <- IO {
                               MDC.put(key2, value2)
                               (Thread.currentThread().getName, MDC.get(keyValuePair.key))
                             }.evalOn(ec)
            (insideThread, insideGot) = inside
            after  <- IO(MDC.get(keyValuePair.key))
            after2 <- IO(MDC.get(key2))
          } yield Result.all(
            List(
              (insideThread ==== evalOnThreadName).log("evalOn did not run on the dedicated thread"),
              Result
                .assert(insideThread =!= computeThread)
                .log(s"evalOn thread ($insideThread) should differ from the compute thread ($computeThread)"),
              (insideGot ==== keyValuePair.value).log("MDC value was lost when crossing into evalOn"),
              (after ==== keyValuePair.value).log("MDC value was lost after returning from evalOn"),
              (after2 ==== value2).log("MDC value put inside evalOn was lost after returning to the compute pool"),
            )
          )
        }(executor => IO(executor.shutdown()))
        .unsafeRunSync()
    }

  /* IO.blocking runs the fiber's blocking region on (or as) a blocking thread. This is
   * where fiber-UNAWARE library code usually lives (JDBC drivers, HTTP clients, ...),
   * and such code often writes MDC itself. Both directions matter: a value put before
   * must be visible inside the blocking region, and a value put inside it (as a library
   * would) must persist in the fiber after the region ends. */
  def testPropagationAcrossBlocking: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      val key2   = keyValuePair.key + "-inside-blocking"
      val value2 = keyValuePair.value + "-inside-blocking"

      (for {
        _      <- IO(MDC.put(keyValuePair.key, keyValuePair.value))
        inside <- IO.blocking {
                    MDC.put(key2, value2)
                    MDC.get(keyValuePair.key)
                  }
        after2 <- IO(MDC.get(key2))
      } yield Result.all(
        List(
          (inside ==== keyValuePair.value).log("MDC value was lost when crossing into IO.blocking"),
          (after2 ==== value2).log("MDC value put inside IO.blocking was lost after returning"),
        )
      ))
        .unsafeRunSync()
    }

  /* The per-request isolation property: every fiber writes to the SAME key and then
   * crosses async boundaries, so the fibers get interleaved across the shared worker
   * threads. Each fiber must still read back its own value — IOLocal state is copied
   * when a fiber is forked, so concurrent fibers are isolated by construction. A plain
   * ThreadLocal adapter fails here, because fibers taking turns on the same worker
   * thread would overwrite and read each other's values. */
  def testConcurrentFibersIsolation: Property =
    for {
      keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
    } yield {
      val sharedKey = "shared-key"

      keyValuePairs
        .keyValuePairs
        .parTraverse { keyValue =>
          for {
            _   <- IO(MDC.put(sharedKey, keyValue.value)) // every fiber writes the same key
            _   <- IO.cede // force interleaving with the other fibers
            _   <- IO.sleep(1.millis)
            got <- IO(MDC.get(sharedKey)) // must still be this fiber's own value
          } yield (got ==== keyValue.value)
            .log(s"the fiber that put ${keyValue.value} saw another fiber's value: $got")
        }
        .map(Result.all)
        .unsafeRunSync()
    }

  private val loggerNameCounter = new AtomicInteger(0)

  /* End-to-end check through the real logging pipeline (the reason this adapter exists):
   * logger.info inside a fiber must produce a logback event whose MDC map is that
   * fiber's context, even with many fibers logging concurrently under the same key —
   * the "which log line belongs to which request" scenario from the blog post.
   * MdcCapturingAppender snapshots the MDC during append, i.e. synchronously on the
   * logging fiber's thread, which is when logback resolves it. */
  def testLogbackEventCapturesFiberMdc: Property =
    for {
      keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
    } yield {
      val requestIdKey  = "requestId"
      val loggerContext = getLoggerContext
      val appender      = new MdcCapturingAppender
      appender.setContext(loggerContext)
      appender.start()

      /* Hedgehog evaluates this property ~100 times and logback caches loggers by name,
       * so a fresh logger name per evaluation keeps appenders from piling up on one
       * cached logger and mixing events between evaluations. */
      val logger = loggerContext.getLogger(s"mdc-propagation-spec-logger-${loggerNameCounter.incrementAndGet()}")
      logger.addAppender(appender)
      logger.setLevel(Level.INFO)
      logger.setAdditive(false)

      (keyValuePairs
        .keyValuePairs
        .parTraverse { keyValue =>
          for {
            _ <- IO(MDC.put(requestIdKey, keyValue.value))
            _ <- IO.sleep(1.millis)
            _ <- IO(logger.info(s"message-${keyValue.key}"))
          } yield ()
        } *> IO {
        val mdcByMessage = appender.capturedEvents.toMap
        Result.all(
          keyValuePairs.keyValuePairs.map { keyValue =>
            (mdcByMessage.get(s"message-${keyValue.key}").flatMap(_.get(requestIdKey)) ==== keyValue.value.some)
              .log(s"the logback event logged with ${keyValue.value} did not capture the fiber's $requestIdKey")
          }
        )
      })
        .guarantee(IO {
          /* Detach even on failure so later evaluations never log into this appender. */
          val _ = logger.detachAppender(appender)
          appender.stop()
        })
        .unsafeRunSync()
    }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf"))
  private def getLoggerContext: LoggerContext =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

}
