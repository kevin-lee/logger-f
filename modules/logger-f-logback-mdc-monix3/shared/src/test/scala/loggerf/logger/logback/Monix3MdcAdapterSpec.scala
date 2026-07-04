package loggerf.logger.logback

import cats.syntax.all._
import hedgehog._
import hedgehog.runner._
import monix.eval.{Task, TaskLocal}
import org.slf4j.{LoggerFactory, MDC, SetMdcAdapter}

import java.io.{PrintWriter, StringWriter}
import java.time.Instant
import java.util
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2023-07-03
  */
trait Monix3MdcAdapterSpecsOnly {
  import Monix3MdcAdapterSpec._

  /*
   * Task.defaultOptions.enableLocalContextPropagation is the same as
   *   sys.props.put("monix.environment.localContextPropagation", "1")
   */
  implicit val opts: Task.Options = Task.defaultOptions.enableLocalContextPropagation

  @SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.ToString"))
  private val monixMdcAdapter: Monix3MdcAdapter =
    try {
      Monix3MdcAdapter.initialize()
    } catch {
      case NonFatal(ex) =>
        val writer = new StringWriter()
        val out    = new PrintWriter(writer)
        ex.printStackTrace(out)
        System
          .err
          .println(
            s"""Error when initializing Monix3MdcAdapter: ${writer.toString}
               |""".stripMargin
          )
        throw ex // scalafix:ok DisableSyntax.throw
    }

  def tests: List[Test] = List(
    property("Task - MDC should be able to put and get a value", testPutAndGet),
    property("Task - MDC should be able to put and get multiple values concurrently", testPutAndGetMultiple),
    property(
      "Task - MDC should be able to put and get with isolated nested modifications",
      testPutAndGetMultipleIsolatedNestedModifications,
    ),
    property("Task - MDC: It should be able to set a context map", testSetContextMap),
    property("Task - MDC should be able to remove the value for the existing key", testRemove),
    property("Task - MDC should be able to remove the multiple values for the existing keys", testRemoveMultiple),
    property(
      "Task - MDC should be able to remove with isolated nested modifications",
      testRemoveMultipleIsolatedNestedModifications,
    ),
    property("Task - MDC: It should return context map for getCopyOfContextMap", testGetCopyOfContextMap),
    property("Task - MDC: It should return context map for getPropertyMap", testGetPropertyMap),
    property("Task - MDC: It should return context map for getKeys", testGetKeys),
    property("test SetMdcAdapter for MDC", testSetMdcAdapterForMDC),
    property("test LoggerContext.setMDCAdapter", testLoggerContextSetMdcAdapter),
  )

  def before(): Unit =
    MDC.clear()

  def putAndGet(key: String, value: String): Task[String] =
    for {
      _   <- Task(MDC.put(key, value))
      got <- Task(MDC.get(key))
    } yield got

  def testPutAndGet: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val task = putAndGet(keyValuePair.key, keyValuePair.value)
      task
        .map(_ ==== keyValuePair.value)
        .runSyncUnsafe()
    }

  def testPutAndGetMultiple: Property = for {
    keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")

  } yield {
    implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
    before()

    val tasks = keyValuePairs.keyValuePairs.map { keyValue =>
      TaskLocal.isolate(putAndGet(keyValue.key, keyValue.value).executeAsync)
    }

    val task = Task.parSequence(tasks)
    task
      .map { retrievedKeyValues =>
        Result.all(
          List(
            retrievedKeyValues.length ==== keyValuePairs.keyValuePairs.length,
            retrievedKeyValues ==== keyValuePairs.keyValuePairs.map(_.value),
          )
        )
      }
      .runSyncUnsafe()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testPutAndGetMultipleIsolatedNestedModifications: Property =
    for {
      a <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("1:" + _).log("a")
      b <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("2:" + _).log("b")
      c <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("3:" + _).log("c")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val test = for {
        _              <- Task(MDC.put("key-1", a))
        before         <- Task(MDC.get("key-1") ==== a)
        beforeIsolated <- TaskLocal.isolate(Task(MDC.get("key-1") ==== a))

        isolated1 <- TaskLocal.isolate {
                       Task(MDC.get("key-1") ==== a)
                         .flatMap { isolated1Before =>
                           Task(MDC.put("key-1", b)) *> Task(
                             (isolated1Before, MDC.get("key-1") ==== b)
                           )
                         }
                     }
        (isolated1Before, isolated1After) = isolated1
        isolated2 <- TaskLocal.isolate {
                       Task(MDC.get("key-1") ==== a)
                         .flatMap { isolated2Before =>
                           Task(MDC.put("key-2", c)) *> Task(
                             (isolated2Before, MDC.get("key-2") ==== c)
                           )
                         }
                     }
        (isolated2Before, isolated2After) = isolated2
      } yield Result.all(
        List(
          before.log("before"),
          beforeIsolated.log("beforeIsolated"),
          isolated1Before.log("isolated1Before"),
          isolated1After.log("isolated1After"),
          isolated2Before.log("isolated2Before"),
          isolated2After.log("isolated2After"),
          (MDC.get("key-1") ==== a).log(s"""After: MDC.get("key-1") is not $a"""),
          (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null"""), // scalafix:ok DisableSyntax.null
        )
      )

      test.runSyncUnsafe()
    }

  def testSetContextMap: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _      <- Task(MDC.put(staticFieldName, staticValueName))
          before <- Task {
                      Result.all(
                        List(
                          (Option(MDC.get("uuid")) ==== none).log("uuid should not be found"),
                          (Option(MDC.get("idNum")) ==== none).log("idNum should not be found"),
                          (Option(MDC.get("timestamp")) ==== none).log("timestamp should not be found"),
                          (Option(MDC.get("someValue")) ==== none).log("someValue should not be found"),
                          (MDC.get(staticFieldName) ==== staticValueName)
                            .log(s"staticFieldName is not $staticValueName"),
                          (Option(MDC.get("random")) ==== none).log("random should not be found"),
                        )
                      )
                    }
          _      <- Task(MDC.setContextMap(productToMap(someContext).asJava))
          now = Instant.now().toString
          _      <- Task(MDC.put("timestamp", now))
          _      <- TaskLocal.isolate(Task(MDC.put("idNum", "ABC")))
          result <- Task {
                      Result.all(
                        List(
                          before,
                          (MDC.get("uuid") ==== someContext.uuid.toString).log("uuid doesn't match"),
                          (MDC.get("idNum") ==== someContext.idNum.toString).log("idNum doesn't match"),
                          (MDC.get("timestamp") ==== now).log("timestamp doesn't match"),
                          (MDC.get("someValue") ==== someContext.someValue.toString).log("someValue doesn't match"),
                          (Option(MDC.get(staticFieldName)) ==== none).log("staticFieldName should not be found"),
                          (Option(MDC.get("random")) ==== none).log("random should not be found"),
                        )
                      )
                    }
        } yield result
      }
      result.runSyncUnsafe()
    }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testRemove: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      (for {
        valueBefore <- putAndGet(keyValuePair.key, keyValuePair.value)

        _          <- Task(MDC.remove(keyValuePair.key))
        valueAfter <- Task(MDC.get(keyValuePair.key))
      } yield Result
        .all(
          List(
            valueBefore ==== keyValuePair.value,
            valueAfter ==== null, // scalafix:ok DisableSyntax.null
          )
        ))
        .runSyncUnsafe()
    }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testRemoveMultiple: Property = for {
    keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
  } yield {
    implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
    before()

    val tasks = keyValuePairs.keyValuePairs.map { keyValue =>
      TaskLocal.isolate(putAndGet(keyValue.key, keyValue.value).executeAsync)
    }

    (for {
      retrievedKeyValues             <- Task.parSequence(tasks)
      retrievedKeyValuesBeforeRemove <- keyValuePairs.keyValuePairs.traverse { keyValue =>
                                          Task(MDC.get(keyValue.key))
                                        }
      _                              <- Task.parSequence(keyValuePairs.keyValuePairs.map { keyValue =>
                                          TaskLocal.isolate(Task(MDC.remove(keyValue.key)))
                                        })
      retrievedKeyValuesAfterRemove  <- keyValuePairs.keyValuePairs.traverse { keyValue =>
                                          Task(MDC.get(keyValue.key))
                                        }
    } yield {
      Result.all(
        List(
          (retrievedKeyValues.length ==== keyValuePairs.keyValuePairs.length).log("retrievedKeyValues.length check"),
          (retrievedKeyValues ==== keyValuePairs.keyValuePairs.map(_.value)).log("retrievedKeyValues value check"),
          (retrievedKeyValuesBeforeRemove.length ==== keyValuePairs.keyValuePairs.length)
            .log("retrievedKeyValuesBeforeRemove.length check"),
          (retrievedKeyValuesBeforeRemove ==== keyValuePairs.keyValuePairs.as(null)) // scalafix:ok DisableSyntax.null
            .log("retrievedKeyValuesBeforeRemove value check"),
          (retrievedKeyValuesAfterRemove.length ==== keyValuePairs.keyValuePairs.length)
            .log("retrievedKeyValuesAfterRemove.length check"),
          (retrievedKeyValuesAfterRemove ==== keyValuePairs.keyValuePairs.as(null)) // scalafix:ok DisableSyntax.null
            .log("retrievedKeyValuesAfterRemove value check"),
        )
      )
    })
      .runSyncUnsafe()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null", "scalafix:DisableSyntax.null"))
  def testRemoveMultipleIsolatedNestedModifications: Property =
    for {
      a <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("1:" + _).log("a")
      b <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("2:" + _).log("b")
      c <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("3:" + _).log("c")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val test = for {
        _              <- Task(MDC.put("key-1", a))
        before         <- Task(MDC.get("key-1") ==== a)
        beforeIsolated <- TaskLocal.isolate(Task(MDC.get("key-1") ==== a))

        isolated1 <- TaskLocal.isolate {
                       Task(MDC.get("key-1") ==== a)
                         .flatMap { isolated1Before =>
                           Task(MDC.put("key-1", b)) *> Task(
                             (isolated1Before, MDC.get("key-1") ==== b)
                           )
                         }
                     }
        (isolated1Before, isolated1After) = isolated1
        isolated2 <- TaskLocal.isolate {
                       Task(MDC.get("key-1") ==== a)
                         .flatMap { isolated2Before =>
                           Task(MDC.put("key-2", c)) *> Task(
                             (isolated2Before, MDC.get("key-2") ==== c)
                           )
                         }
                     }
        (isolated2Before, isolated2After) = isolated2
        isolated3 <- TaskLocal.isolate {
                       for {
                         isolated2Key1Before      <- Task(MDC.get("key-1")).map(_ ==== a)
                         isolated2Key2Before      <-
                           Task(MDC.get("key-2")).map(_ ==== null)
                         _                        <- Task(MDC.put("key-2", c))
                         isolated2Key2After       <- Task(MDC.get("key-2")).map(_ ==== c)
                         _                        <- Task(MDC.remove("key-2"))
                         isolated2Key2AfterRemove <-
                           Task(MDC.get("key-2")).map(_ ==== null)
                       } yield (
                         isolated2Key1Before,
                         isolated2Key2Before,
                         isolated2Key2After,
                         isolated2Key2AfterRemove,
                       )

                     }
        (isolated2Key1Before, isolated2Key2Before, isolated2Key2After, isolated2Key2AfterRemove) = isolated3
        key1After = (MDC.get("key-1") ==== a).log(s"""After: MDC.get("key-1") is not $a""")
        key2After =
          (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null""")

        _ <- Task(MDC.remove("key-1"))
        key1AfterRemove = (MDC.get("key-1") ==== null)
                            .log("""After Remove: MDC.get("key-1") is not null""")
      } yield Result.all(
        List(
          before.log("before"),
          beforeIsolated.log("beforeIsolated"),
          isolated1Before.log("isolated1Before"),
          isolated1After.log("isolated1After"),
          isolated2Before.log("isolated2Before"),
          isolated2After.log("isolated2After"),
          isolated2Key1Before,
          isolated2Key2Before,
          isolated2Key2After,
          isolated2Key2AfterRemove,
          key1After,
          key2After,
          key1AfterRemove,
        )
      )

      test.runSyncUnsafe()
    }

  def testGetCopyOfContextMap: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _         <- Task(MDC.put(staticFieldName, staticValueName))
          mapBefore <- Task(MDC.getCopyOfContextMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== Map(staticFieldName -> staticValueName))
                             .log("propertyMap Before")
                         }
          expectedPropertyMap = productToMap(someContext)
          _         <- Task(MDC.setContextMap(expectedPropertyMap.asJava))
          mapAfter  <- Task(MDC.getCopyOfContextMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== expectedPropertyMap).log("propertyMap After")
                         }
          now = Instant.now().toString
          _         <- Task(MDC.put("timestamp", now))
          _         <- TaskLocal.isolate(Task(MDC.put("idNum", "ABC")))
          mapAfter2 <- Task(MDC.getCopyOfContextMap)
                         .map(propertyMap =>
                           (propertyMap.asScala.toMap ==== expectedPropertyMap.updated("timestamp", now))
                             .log("propertyMap After 2")
                         )
        } yield Result.all(
          List(
            mapBefore,
            mapAfter,
            mapAfter2,
          )
        )
      }
      result.runSyncUnsafe()
    }

  def testGetPropertyMap: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _         <- Task(MDC.put(staticFieldName, staticValueName))
          mapBefore <- Task(monixMdcAdapter.getPropertyMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== Map(staticFieldName -> staticValueName))
                             .log("propertyMap Before")
                         }
          expectedPropertyMap = productToMap(someContext)
          _         <- Task(MDC.setContextMap(expectedPropertyMap.asJava))
          mapAfter  <- Task(monixMdcAdapter.getPropertyMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== expectedPropertyMap).log("propertyMap After")
                         }
          now = Instant.now().toString
          _         <- Task(MDC.put("timestamp", now))
          _         <- TaskLocal.isolate(Task(MDC.put("idNum", "ABC")))
          mapAfter2 <- Task(monixMdcAdapter.getPropertyMap)
                         .map(propertyMap =>
                           (propertyMap.asScala.toMap ==== expectedPropertyMap.updated("timestamp", now))
                             .log("propertyMap After 2")
                         )
        } yield Result.all(
          List(
            mapBefore,
            mapAfter,
            mapAfter2,
          )
        )
      }
      result.runSyncUnsafe()
    }

  def testGetKeys: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced
      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _      <- Task(MDC.put(staticFieldName, staticValueName))
          keySet <- Task(monixMdcAdapter.getKeys)
                      .map { keySet =>
                        (keySet.asScala.toSet ==== Set(staticFieldName))
                          .log("keySet Before")
                      }
          expectedPropertyMap = productToMap(someContext)
          expectedKeySet      = expectedPropertyMap.keySet
          _           <- Task(MDC.setContextMap(expectedPropertyMap.asJava))
          keySetAfter <- Task(monixMdcAdapter.getKeys)
                           .map { keySet =>
                             (keySet.asScala.toSet ==== expectedKeySet).log("keySet After")
                           }
          now = Instant.now().toString
          _            <- Task(MDC.put("timestamp", now))
          _            <- TaskLocal.isolate(Task(MDC.put("idNum", "ABC")))
          keySetAfter2 <- Task(monixMdcAdapter.getKeys)
                            .map(keySet =>
                              (keySet.asScala.toSet ==== expectedKeySet)
                                .log("keySet After 2")
                            )
        } yield Result.all(
          List(
            keySet,
            keySetAfter,
            keySetAfter,
            keySetAfter2,
          )
        )
      }
      result.runSyncUnsafe()
    }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  private def productToMap[A <: Product](product: A): Map[String, String] = {
    // This doesn't work for Scala 2.12
    //    val fields = product.productElementNames.toVector

    val fields = product.getClass.getDeclaredFields.map(_.getName)
    val length = product.productArity

    val fieldAndValueParis = for {
      n <- 0 until length
      maybeValue = product.productElement(n) match {
                     case maybe @ (Some(_) | None) => maybe
                     case value => Option(value)
                   }
    } yield (fields(n), maybeValue)
    fieldAndValueParis.collect {
      case (name, Some(value)) =>
        name -> value.toString
    }.toMap
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSetMdcAdapterForMDC: Property = for {
    mdcAdapterName1 <- Gen.string(Gen.ascii, Range.linear(5, 5)).log("mdcAdapterName1")
    mdcAdapterName2 <- Gen.string(Gen.ascii, Range.linear(5, 5)).log("mdcAdapterName2")
    mdcAdapterName  <- Gen.constant(mdcAdapterName1 + mdcAdapterName2).log("mdcAdapterName")
  } yield {
    val mdcAdapter = TestMdcAdapter(mdcAdapterName)
    val before     = MDC.getMDCAdapter()

    SetMdcAdapter(mdcAdapter)

    val after = MDC.getMDCAdapter()

    Result.all(
      List(
        Result.diffNamed("before should not be equal to the new MDCAdapter", before, mdcAdapter)(_ != _),
        (after ==== mdcAdapter).log("after SetMdcAdapter should be equal to the new MDCAdapter"),
      )
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testLoggerContextSetMdcAdapter: Property = for {
    mdcAdapterName1 <- Gen.string(Gen.ascii, Range.linear(5, 5)).log("mdcAdapterName1")
    mdcAdapterName2 <- Gen.string(Gen.ascii, Range.linear(5, 5)).log("mdcAdapterName2")
    mdcAdapterName  <- Gen.constant(mdcAdapterName1 + mdcAdapterName2).log("mdcAdapterName")
  } yield {
    val mdcAdapter    = TestMdcAdapter(mdcAdapterName)
    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val loggerContext =
      LoggerFactory
        .getILoggerFactory
        .asInstanceOf[ch.qos.logback.classic.LoggerContext] // scalafix:ok DisableSyntax.asInstanceOf

    val before = loggerContext.getMDCAdapter()

    val _ = Monix3MdcAdapter.initializeWithMonix3MdcAdapterAndLoggerContext(mdcAdapter, loggerContext)

    val after = loggerContext.getMDCAdapter()

    Result.all(
      List(
        Result.diffNamed("before should not be equal to the new MDCAdapter", before, mdcAdapter)(_ != _),
        (after ==== mdcAdapter).log("after SetMdcAdapter should be equal to the new MDCAdapter"),
      )
    )
  }
}
object Monix3MdcAdapterSpec extends Properties with Monix3MdcAdapterSpecsOnly {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class TestMdcAdapter(name: String) extends Monix3MdcAdapter {

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def put(key: String, `val`: String): Unit = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def get(key: String): String = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def remove(key: String): Unit = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def clear(): Unit = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def getCopyOfContextMap: util.Map[String, String] = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def setContextMap0(contextMap: util.Map[String, String]): Unit = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def pushByKey(key: String, value: String): Unit = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def popByKey(key: String): String = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def getCopyOfDequeByKey(key: String): util.Deque[String] = ???

    @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
    override def clearDequeByKey(key: String): Unit = ???
  }
}
