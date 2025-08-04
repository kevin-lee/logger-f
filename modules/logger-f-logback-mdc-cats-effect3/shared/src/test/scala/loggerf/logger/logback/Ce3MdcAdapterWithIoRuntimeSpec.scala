package loggerf.logger.logback

import cats.effect._
import cats.effect.unsafe.IORuntime
import cats.syntax.all._
import hedgehog._
import hedgehog.runner._
import org.slf4j.MDC

import java.time.Instant
import scala.jdk.CollectionConverters._

/** @author Kevin Lee
  * @since 2023-07-07
  */
object Ce3MdcAdapterWithIoRuntimeSpec extends Properties {
  private val oldValue = sys.props.put("cats.effect.trackFiberContext", "true")
  println(
    s"${this.getClass.getSimpleName.stripSuffix("$")}[B] cats.effect.trackFiberContext=${oldValue.getOrElse("")}"
  )
  println(
    s"${this.getClass.getSimpleName.stripSuffix("$")}[A] cats.effect.trackFiberContext=${sys.props.getOrElse("cats.effect.trackFiberContext", "")}"
  )

  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global

  private val ce3MdcAdapter: Ce3MdcAdapterWithIoRuntime = Ce3MdcAdapterWithIoRuntime.initialize()

  override def tests: List[Test] = List(
    property("IO - MDC should be able to put and get a value", testPutAndGet),
    property("IO - MDC should be able to put and get multiple values concurrently", testPutAndGetMultiple),
    property(
      "IO - MDC should be able to put and get with isolated nested modifications",
      testPutAndGetMultipleIsolatedNestedModifications,
    ),
    property(
      "IO - MDC should be able to put and get with isolated nested modifications - more complex case",
      testPutAndGetMultipleIsolatedNestedModifications2,
    ),
    property("IO - MDC: It should be able to set a context map", testSetContextMap),
    property("IO - MDC should be able to remove the value for the existing key", testRemove),
    property("IO - MDC should be able to remove the multiple values for the existing keys", testRemoveMultiple),
    property(
      "IO - MDC should be able to remove with isolated nested modifications",
      testRemoveMultipleIsolatedNestedModifications,
    ),
    property("IO - MDC: It should return context map for getCopyOfContextMap", testGetCopyOfContextMap),
    property("IO - MDC: It should return context map for getPropertyMap", testGetPropertyMap),
    property("IO - MDC: It should return context map for getKeys", testGetKeys),
  )

  def before(): Unit = MDC.clear()

  def putAndGet(key: String, value: String): IO[String] =
    for {
      _   <- IO(MDC.put(key, value))
      got <- IO(MDC.get(key))
    } yield got

  def testPutAndGet: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      before()

      val io = putAndGet(keyValuePair.key, keyValuePair.value)
      io.map(_ ==== keyValuePair.value)
        .unsafeRunSync()
    }

  def testPutAndGetMultiple: Property = for {
    keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")

  } yield {
    before()

    val ios = keyValuePairs.keyValuePairs.traverse { keyValue =>
      putAndGet(keyValue.key, keyValue.value).start
    }

    (for {
      fibers             <- ios
      retrievedKeyValues <- fibers.traverse(_.joinWithNever)
    } yield {
      Result.all(
        List(
          retrievedKeyValues.length ==== keyValuePairs.keyValuePairs.length,
          retrievedKeyValues ==== keyValuePairs.keyValuePairs.map(_.value),
        )
      )
    })
      .unsafeRunSync()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testPutAndGetMultipleIsolatedNestedModifications: Property =
    for {
      a <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("1:" + _).log("a")
      b <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("2:" + _).log("b")
      c <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("3:" + _).log("c")
    } yield {

      before()

      val beforeSet      = (MDC.get("key-1") ==== null).log("before set") // scalafix:ok DisableSyntax.null
      MDC.put("key-1", a)
      val afterBeforeSet = (MDC.get("key-1") ==== a).log("""after beforeSet: `MDC.get("key-1") ==== a` failed""")

      val test = for {
//        _              <- IO(MDC.put("key-1", a))
        before         <- IO((MDC.get("key-1") ==== a).log("before"))
        beforeIsolated <- IO((MDC.get("key-1") ==== a).log("beforeIsolated"))
                            .start
                            .flatMap(_.joinWithNever)

        isolated1 <- (
                       IO((MDC.get("key-1") ==== a).log("isolated1Before")).flatMap { isolated1Before =>
                         IO(MDC.put("key-1", b)) *> IO(
                           (isolated1Before, (MDC.get("key-1") ==== b).log("isolated1After"))
                         )
                       }
                     ).start
        isolated2 <- (
                       IO((MDC.get("key-1") ==== a).log("isolated2Before")).flatMap { isolated2Before =>
                         IO(MDC.put("key-2", c)) *> IO(
                           (isolated2Before, (MDC.get("key-2") ==== c).log("isolated2After"))
                         )
                       }
                     ).start

        joinedIsolated1 <- isolated1.joinWithNever
        joinedIsolated2 <- isolated2.joinWithNever
        (isolated1Before, isolated1After) = joinedIsolated1
        (isolated2Before, isolated2After) = joinedIsolated2
        key1Result <- IO((MDC.get("key-1") ==== a).log(s"""After: MDC.get("key-1") is not $a"""))
        key2Result <- IO(
                        (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null""")
                      ) // scalafix:ok DisableSyntax.null
      } yield Result.all(
        List(
          beforeSet,
          afterBeforeSet,
          before,
          beforeIsolated,
          isolated1Before,
          isolated1After,
          isolated2Before,
          isolated2After,
          key1Result,
          key2Result,
          //          (MDC.get("key-1") ==== a).log(s"""${Thread.currentThread().getName}:After: MDC.get("key-1") is not $a"""),
          //          (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null"""), // scalafix:ok DisableSyntax.null
        )
      )

      test.unsafeRunSync()
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

      before()

      val beforeSet      = (MDC.get("key-1") ==== null).log("before set") // scalafix:ok DisableSyntax.null
      MDC.put("key-1", a2)
      val afterBeforeSet =
        (MDC.get("key-1") ==== a2)
          .log(s"""after beforeSet: MDC.get("key-1") should be $a2""") // scalafix:ok DisableSyntax.null

      val test = for {
        beforeSet1     <- IO((MDC.get("key-1") ==== a2).log("before set2")) // scalafix:ok DisableSyntax.null
        _              <- IO(MDC.put("key-1", a))
        beforeSet2     <- IO(
                            (MDC.get("key-1") ==== a).log(s"""before set2: MDC.get("key-1") should be $a""")
                          ) // scalafix:ok DisableSyntax.null
        _              <- IO(MDC.put("key-1", a))
        before         <-
          IO {
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
        beforeIsolated <- IO {
                            val actual = MDC.get("key-1")
                            (actual ==== a).log(s"""beforeIsolated: MDC.get("key-1") should be $a, but it is $actual""")
                          }
                            .start
                            .flatMap(_.joinWithNever)

        isolated1 <-
          (
            IO {
              val actual = MDC.get("key-1")
              (actual ==== a).log(s"""isolated1Before: MDC.get("key-1") should be $a, but it is $actual""")
            }.flatMap { isolated1Before =>
              IO(
                MDC.put("key-1", b1)
              ) *> IO {
                val actual = MDC.get("key-1")
                List(
                  isolated1Before,
                  (actual ==== b1).log(s"""isolated1After: MDC.get("key-1") should be $b1, but it is $actual"""),
                )
              }
            }
          ).start
        isolated2 <-
          (
            IO {
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
              IO(
                MDC.put("key-2", c2)
              ) *> IO {
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
          ).start
        isolated3 <-
          (
            IO {
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
              IO(
                MDC.put("key-1", b3)
              ) *> IO(
                MDC.put("key-2", c3)
              ) *> IO(
                MDC.put("key-3", a3)
              ) *> IO {
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
          ).start

        joinedIsolated1 <- isolated1.joinWithNever
        joinedIsolated2 <- isolated2.joinWithNever
        joinedIsolated3 <- isolated3.joinWithNever

        key1Result <- IO((MDC.get("key-1") ==== a).log(s"""After: MDC.get("key-1") is not $a"""))
        key2Result <- IO(
                        (MDC.get("key-2") ==== null).log("""After: MDC.get("key-2") is not null""")
                      ) // scalafix:ok DisableSyntax.null
        key3Result <- IO(
                        (MDC.get("key-3") ==== null).log("""After: MDC.get("key-3") is not null""")
                      ) // scalafix:ok DisableSyntax.null
      } yield List(
        beforeSet,
        afterBeforeSet,
        beforeSet1,
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

//      val afterIo = (MDC.get("key-1") ==== a2).log(s"""after IO: MDC.get("key-1") should be $a2""")
      Result.all(
        test.unsafeRunSync() ++
          List(
//            afterIo
          )
      )
    }

  def testSetContextMap: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {

      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _      <- IO(MDC.put(staticFieldName, staticValueName))
          before <- IO {
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
          _      <- IO(MDC.setContextMap(productToMap(someContext).asJava))
          now = Instant.now().toString
          _      <- IO(MDC.put("timestamp", now))
          _      <- IO(MDC.put("idNum", "ABC")).start.flatMap(_.joinWithNever)
          result <- IO {
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
      result.unsafeRunSync()
    }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testRemove: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      before()

      (for {
        valueBefore <- putAndGet(keyValuePair.key, keyValuePair.value)

        _          <- IO(MDC.remove(keyValuePair.key))
        valueAfter <- IO(MDC.get(keyValuePair.key))
      } yield Result
        .all(
          List(
            valueBefore ==== keyValuePair.value,
            valueAfter ==== null, // scalafix:ok DisableSyntax.null
          )
        ))
        .unsafeRunSync()
    }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testRemoveMultiple: Property = for {
    keyValuePairs <- Gens.genKeyValuePairs.log("keyValuePairs")
  } yield {

    before()

    val ios = keyValuePairs.keyValuePairs.traverse { keyValue =>
      putAndGet(keyValue.key, keyValue.value).start
    }

    (for {
      fibers                         <- ios
      retrievedKeyValues             <- fibers.traverse(_.joinWithNever)
      retrievedKeyValuesBeforeRemove <- keyValuePairs.keyValuePairs.traverse { keyValue =>
                                          IO(MDC.get(keyValue.key))
                                        }
      fibers4Removal                 <- keyValuePairs.keyValuePairs.traverse { keyValue =>
                                          IO(MDC.remove(keyValue.key)).start
                                        }
      _                              <- fibers4Removal.traverse_(_.joinWithNever)
      retrievedKeyValuesAfterRemove  <- keyValuePairs.keyValuePairs.traverse { keyValue =>
                                          IO(MDC.get(keyValue.key))
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
      .unsafeRunSync()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def testRemoveMultipleIsolatedNestedModifications: Property =
    for {
      a <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("1:" + _).log("a")
      b <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("2:" + _).log("b")
      c <- Gen.string(Gen.alpha, Range.linear(1, 10)).map("3:" + _).log("c")
    } yield {

      before()

      val test = for {
        _              <- IO(MDC.put("key-1", a))
        before         <- IO((MDC.get("key-1") ==== a).log("before"))
        beforeIsolated <- IO((MDC.get("key-1") ==== a).log("beforeIsolated")).start.flatMap(_.joinWithNever)

        isolated1 <- (IO((MDC.get("key-1") ==== a).log("isolated1Before"))
                       .flatMap { isolated1Before =>
                         IO(MDC.put("key-1", b)) *> IO(
                           (isolated1Before, (MDC.get("key-1") ==== b).log("isolated1After"))
                         )
                       })
                       .start
                       .flatMap(_.joinWithNever)
        (isolated1Before, isolated1After) = isolated1
        isolated2 <- (IO((MDC.get("key-1") ==== a).log("isolated2Before"))
                       .flatMap { isolated2Before =>
                         IO(MDC.put("key-2", c)) *> IO(
                           (isolated2Before, (MDC.get("key-2") ==== c).log("isolated2After"))
                         )
                       })
                       .start
                       .flatMap(_.joinWithNever)
        (isolated2Before, isolated2After) = isolated2
        isolated3 <- (for {
                       isolated2Key1Before      <- IO(MDC.get("key-1")).map(_ ==== a)
                       isolated2Key2Before      <-
                         IO(MDC.get("key-2")).map(_ ==== null) // scalafix:ok DisableSyntax.null
                       _                        <- IO(MDC.put("key-2", c))
                       isolated2Key2After       <- IO(MDC.get("key-2")).map(_ ==== c)
                       _                        <- IO(MDC.remove("key-2"))
                       isolated2Key2AfterRemove <-
                         IO(MDC.get("key-2")).map(_ ==== null) // scalafix:ok DisableSyntax.null
                     } yield (
                       isolated2Key1Before,
                       isolated2Key2Before,
                       isolated2Key2After,
                       isolated2Key2AfterRemove,
                     )).start.flatMap(_.joinWithNever)
        (isolated2Key1Before, isolated2Key2Before, isolated2Key2After, isolated2Key2AfterRemove) = isolated3
        key1After <- IO((MDC.get("key-1") ==== a).log(s"""After: MDC.get("key-1") is not $a"""))
        key2After <- IO(
                       (MDC.get("key-2") ==== null) // scalafix:ok DisableSyntax.null
                         .log("""After: MDC.get("key-2") is not null""")
                     )

        _ <- IO(MDC.remove("key-1"))
        key1AfterRemove = (MDC.get("key-1") ==== null)
                            .log("""After Remove: MDC.get("key-1") is not null""") // scalafix:ok DisableSyntax.null
      } yield Result.all(
        List(
          before,
          beforeIsolated,
          isolated1Before,
          isolated1After,
          isolated2Before,
          isolated2After,
          isolated2Key1Before,
          isolated2Key2Before,
          isolated2Key2After,
          isolated2Key2AfterRemove,
          key1After,
          key2After,
          key1AfterRemove,
        )
      )

      test.unsafeRunSync()
    }

  def testGetCopyOfContextMap: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {

      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _         <- IO(MDC.put(staticFieldName, staticValueName))
          mapBefore <- IO(MDC.getCopyOfContextMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== Map(staticFieldName -> staticValueName))
                             .log("propertyMap Before")
                         }
          expectedPropertyMap = productToMap(someContext)
          _         <- IO(MDC.setContextMap(expectedPropertyMap.asJava))
          mapAfter  <- IO(MDC.getCopyOfContextMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== expectedPropertyMap).log("propertyMap After")
                         }
          now = Instant.now().toString
          _         <- IO(MDC.put("timestamp", now))
          _         <- IO(MDC.put("idNum", "ABC")).start.flatMap(_.joinWithNever)
          mapAfter2 <- IO(MDC.getCopyOfContextMap)
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
      result.unsafeRunSync()
    }

  def testGetPropertyMap: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {

      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _         <- IO(MDC.put(staticFieldName, staticValueName))
          mapBefore <- IO(ce3MdcAdapter.getPropertyMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== Map(staticFieldName -> staticValueName))
                             .log("propertyMap Before")
                         }
          expectedPropertyMap = productToMap(someContext)
          _         <- IO(MDC.setContextMap(expectedPropertyMap.asJava))
          mapAfter  <- IO(ce3MdcAdapter.getPropertyMap)
                         .map { propertyMap =>
                           (propertyMap.asScala.toMap ==== expectedPropertyMap).log("propertyMap After")
                         }
          now = Instant.now().toString
          _         <- IO(MDC.put("timestamp", now))
          _         <- IO(MDC.put("idNum", "ABC")).start.flatMap(_.joinWithNever)
          mapAfter2 <- IO(ce3MdcAdapter.getPropertyMap)
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
      result.unsafeRunSync()
    }

  def testGetKeys: Property =
    for {
      someContext <- Gens.genSomeContext.log("someContext")
    } yield {

      before()

      val staticFieldName = "staticFieldName"
      val staticValueName = "staticValueName"

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val result = {
        for {
          _            <- IO(MDC.put(staticFieldName, staticValueName))
          keySetBefore <- IO(ce3MdcAdapter.getKeys)
                            .map { keySet =>
                              (keySet.asScala.toSet ==== Set(staticFieldName))
                                .log("keySet Before")
                            }
          expectedPropertyMap = productToMap(someContext)
          expectedKeySet      = expectedPropertyMap.keySet
          _           <- IO(MDC.setContextMap(expectedPropertyMap.asJava))
          keySetAfter <- IO(ce3MdcAdapter.getKeys)
                           .map { keySet =>
                             (keySet.asScala.toSet ==== expectedKeySet).log("keySet After")
                           }
          now = Instant.now().toString
          _            <- IO(MDC.put("timestamp", now))
          _            <- IO(MDC.put("idNum", "ABC")).start.flatMap(_.joinWithNever)
          keySetAfter2 <- IO(ce3MdcAdapter.getKeys)
                            .map(keySet =>
                              (keySet.asScala.toSet ==== expectedKeySet)
                                .log("keySet After 2")
                            )
        } yield Result.all(
          List(
            keySetBefore,
            keySetAfter,
            keySetAfter,
            keySetAfter2,
          )
        )
      }
      result.unsafeRunSync()
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
