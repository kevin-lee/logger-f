package loggerf.logger.logback

import cats.syntax.all._
import hedgehog._
import hedgehog.runner._
import monix.eval.{Task, TaskLocal}
import org.slf4j.MDC

import java.time.Instant
import scala.jdk.CollectionConverters._

/** @author Kevin Lee
  * @since 2023-07-03
  */
object Monix3MdcAdapterSpec extends Properties {

  /*
   * Task.defaultOptions.enableLocalContextPropagation is the same as
   *   sys.props.put("monix.environment.localContextPropagation", "1")
   */
  implicit val opts: Task.Options = Task.defaultOptions.enableLocalContextPropagation

  private val monixMdcAdapter: Monix3MdcAdapter = Monix3MdcAdapter.initialize()

  override def tests: List[Test] = List(
    property("Task - MDC should be able to put and get a value", testPutAndGet),
    property("Task - MDC should be able to put and get multiple values concurrently", testPutAndGetMultiple),
    property(
      "Task - MDC should be able to put and get with isolated nested modifications",
      testPutAndGetMultipleIsolatedNestedModifications,
    ),
    property(
      "Task - MDC should be able to put and get with isolated nested modifications - more complex case",
      testPutAndGetMultipleIsolatedNestedModifications2,
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

      val beforeSet = (MDC.get("key-1") ==== null).log("before set") // scalafix:ok DisableSyntax.null
      MDC.put("key-1", a)

      val test = for {
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
          beforeSet,
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

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testPutAndGetMultipleIsolatedNestedModifications2: Property =
    for {
      a  <- Gen.string(Gen.alpha, Range.linear(1, 2)).map("a:" + _).log("a")
      a2 <- Gen.string(Gen.alpha, Range.linear(1, 2)).map("a2:" + a + _).log("a2")
      b1 <- Gen.string(Gen.alpha, Range.linear(3, 4)).map("b1:" + _).log("b1")
      c2 <- Gen.string(Gen.alpha, Range.linear(5, 6)).map("c1:" + _).log("c1")
      a3 <- Gen.string(Gen.alpha, Range.linear(7, 8)).map("a3:" + _).log("a3")
      b3 <- Gen.string(Gen.alpha, Range.linear(9, 10)).map("b3:" + _).log("b3")
      c3 <- Gen.string(Gen.alpha, Range.linear(11, 12)).map("c3:" + _).log("c3")
    } yield {
      implicit val scheduler: monix.execution.Scheduler = monix.execution.Scheduler.traced

      before()

      val beforeSet         = (MDC.get("key-1") ==== null).log("before set") // scalafix:ok DisableSyntax.null
      MDC.put("key-1", a2)
      val beforeAndAfterSet =
        (MDC.get("key-1") ==== a2).log(s"""before: MDC.get("key-1") should be $a2""") // scalafix:ok DisableSyntax.null

      val test = for {
        beforeSet2     <- Task((MDC.get("key-1") ==== a2).log("before set2")) // scalafix:ok DisableSyntax.null
        _              <- Task(MDC.put("key-1", a))
        before         <-
          Task {
            val actual1 = MDC.get("key-1")
            val actual2 = MDC.get("key-2")
            val actual3 = MDC.get("key-3")
            List(
              (actual1 ==== a).log(s"""before: MDC.get("key-1") should be $a, but it is $actual1"""),
              (actual2 ==== null).log(
                s"""before: MDC.get("key-2") should be null, but it is $actual2"""
              ), // scalafix:ok DisableSyntax.null
              (actual3 ==== null).log(
                s"""before: MDC.get("key-3") should be null, but it is $actual3"""
              ), // scalafix:ok DisableSyntax.null
            )
          }
        beforeIsolated <- TaskLocal.isolate(Task {
                            val actual = MDC.get("key-1")
                            (actual ==== a).log(s"""beforeIsolated: MDC.get("key-1") should be $a, but it is $actual""")
                          })

        isolated1 <-
          TaskLocal.isolate(
            Task {
              val actual = MDC.get("key-1")
              (actual ==== a).log(s"""isolated1Before: MDC.get("key-1") should be $a, but it is $actual""")
            }.flatMap { isolated1Before =>
              Task(
                MDC.put("key-1", b1)
              ) *> Task {
                val actual = MDC.get("key-1")
                List(
                  isolated1Before,
                  (actual ==== b1).log(s"""isolated1After: MDC.get("key-1") should be $b1, but it is $actual"""),
                )
              }
            }
          )
        isolated2 <-
          TaskLocal.isolate(
            Task {
              val actual1 = MDC.get("key-1")
              val actual2 = MDC.get("key-2")
              val actual3 = MDC.get("key-3")
              List(
                (actual1 ==== a).log(s"""isolated2Before: MDC.get("key-1") should be $a, but it is $actual1"""),
                (actual2 ==== null).log(
                  s"""isolated2Before: MDC.get("key-2") should be null, but it is $actual2"""
                ), // scalafix:ok DisableSyntax.null
                (actual3 ==== null).log(
                  s"""isolated2Before: MDC.get("key-3") should be null, but it is $actual3"""
                ), // scalafix:ok DisableSyntax.null
              )
            }.flatMap { isolated2Before =>
              Task(
                MDC.put("key-2", c2)
              ) *> Task {
                val actual1 = MDC.get("key-1")
                val actual2 = MDC.get("key-2")
                val actual3 = MDC.get("key-3")
                isolated2Before ++
                  List(
                    (actual1 ==== a).log(s"""isolated2After: MDC.get("key-1") should be $a, but it is $actual1"""),
                    (actual2 ==== c2).log(
                      s"""isolated2After: MDC.get("key-2") should be $c2, but it is $actual2"""
                    ), // scalafix:ok DisableSyntax.null
                    (actual3 ==== null).log(
                      s"""isolated2After: MDC.get("key-3") should be null, but it is $actual3"""
                    ), // scalafix:ok DisableSyntax.null
                  )
              }
            }
          )
        isolated3 <-
          TaskLocal.isolate(
            Task {
              val actual1 = MDC.get("key-1")
              val actual2 = MDC.get("key-2")
              val actual3 = MDC.get("key-3")
              List(
                (actual1 ==== a).log(s"""isolated3Before: MDC.get("key-1") should be $a, but it is $actual1"""),
                (actual2 ==== null).log(
                  s"""isolated3Before: MDC.get("key-2") should be null, but it is $actual2"""
                ), // scalafix:ok DisableSyntax.null
                (actual3 ==== null).log(
                  s"""isolated3Before: MDC.get("key-3") should be null, but it is $actual3"""
                ), // scalafix:ok DisableSyntax.null
              )
            }.flatMap { isolated3Before =>
              Task(
                MDC.put("key-1", b3)
              ) *> Task(
                MDC.put("key-2", c3)
              ) *> Task(
                MDC.put("key-3", a3)
              ) *> Task {
                val actual1 = MDC.get("key-1")
                val actual2 = MDC.get("key-2")
                val actual3 = MDC.get("key-3")
                isolated3Before ++ List(
                  (actual1 ==== b3).log(s"""isolated3After: MDC.get("key-1") should be $b3, but it is $actual1"""),
                  (actual2 ==== c3).log(s"""isolated3After: MDC.get("key-2") should be $c3, but it is $actual2"""),
                  (actual3 ==== a3).log(s"""isolated3After: MDC.get("key-3") should be $a3, but it is $actual3"""),
                )
              }
            }
          )

        joinedIsolated1 = isolated1
        joinedIsolated2 = isolated2
        joinedIsolated3 = isolated3

        key1Result <- Task((MDC.get("key-1") ==== a).log(s"""After: MDC.get("key-1") is not $a"""))
        key2Result <- Task(
                        (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null""")
                      ) // scalafix:ok DisableSyntax.null
        key3Result <- Task(
                        (MDC.get("key-3") ==== null).log("""After: MDC.get("key-3") is not null""")
                      ) // scalafix:ok DisableSyntax.null
      } yield List(
        beforeSet,
        beforeAndAfterSet,
        beforeSet2,
      ) ++ before ++ List(
        beforeIsolated
      ) ++
        joinedIsolated1 ++
        joinedIsolated2 ++
        joinedIsolated3 ++ List(
          key1Result,
          key2Result,
          key3Result,
          //          (MDC.get("key-1") ==== a).log(s"""${Thread.currentThread().getName}:After: MDC.get("key-1") is not $a"""),
          //          (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null"""), // scalafix:ok DisableSyntax.null
        )

      val afterIo = MDC.get("key-1") ==== a2
      Result.all(
        test.runSyncUnsafe() ++
          List(
            afterIo
          )
      )
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

}
