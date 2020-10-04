package loggerf.scalaz

import scalaz._
import Scalaz._
import scalaz.effect._
import effectie.scalaz.Effectful._
import effectie.scalaz.EffectConstructor
import hedgehog._
import hedgehog.runner._
import loggerf.logger.LoggerForTesting

/**
 * @author Kevin Lee
 * @since 2020-04-13
 */
object LoggerEitherTSpec extends Properties {
  override def tests: List[Test] = List(
    property("test LoggerEitherT.debugEitherT(EitherT[F, A, B])", testLoggerEitherTDebugEitherTFEAB)
  , property("test LoggerEitherT.infoEitherT(EitherT[F, A, B])", testLoggerEitherTInfoEitherTFEAB)
  , property("test LoggerEitherT.warnEitherT(EitherT[F, A, B])", testLoggerEitherTWarnEitherTFEAB)
  , property("test LoggerEitherT.errorEitherT(EitherT[F, A, B])", testLoggerEitherTErrorEitherTFEAB)
  )


  def testLoggerEitherTDebugEitherTFEAB: Property = for {
    rightInt <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](eab: String \/ Int): F[String \/ Int] =
      LoggerEitherT[F].debugEitherT(EitherT(effectOf(eab)))(a => s"Error: $a", b => b.toString).run

    val eab = if (isRight) rightInt.right[String] else leftString.left[Int]

    val result = runLog[IO](eab).unsafePerformIO()

    val expected = eab match {
      case \/-(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString)
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )

      case -\/(msg) =>
        LoggerForTesting(
          debugMessages = Vector(s"Error: $msg")
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )
    }

    Result.all(
      List(
        (result ==== eab).log("result ==== eab failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testLoggerEitherTInfoEitherTFEAB: Property = for {
    rightInt <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](eab: String \/ Int): F[String \/ Int] =
     LoggerEitherT[F].infoEitherT(EitherT(effectOf(eab)))(a => s"Error: $a", b => b.toString).run

    val eab = if (isRight) rightInt.right[String] else leftString.left[Int]

    val result = runLog[IO](eab).unsafePerformIO()

    val expected = eab match {
      case \/-(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector(n.toString)
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )

      case -\/(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector(s"Error: $msg")
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )
    }

    Result.all(
      List(
        (result ==== eab).log("result ==== eab failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testLoggerEitherTWarnEitherTFEAB: Property = for {
    rightInt <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](eab: String \/ Int): F[String \/ Int] =
     LoggerEitherT[F].warnEitherT(EitherT(effectOf(eab)))(a => s"Error: $a", b => b.toString).run

    val eab = if (isRight) rightInt.right[String] else leftString.left[Int]

    val result = runLog[IO](eab).unsafePerformIO()

    val expected = eab match {
      case \/-(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector(n.toString)
        , errorMessages = Vector.empty
        )

      case -\/(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector(s"Error: $msg")
        , errorMessages = Vector.empty
        )
    }

    Result.all(
      List(
        (result ==== eab).log("result ==== eab failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testLoggerEitherTErrorEitherTFEAB: Property = for {
    rightInt <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](eab: String \/ Int): F[String \/ Int] =
     LoggerEitherT[F].errorEitherT(EitherT(effectOf(eab)))(a => s"Error: $a", b => b.toString).run

    val eab = if (isRight) rightInt.right[String] else leftString.left[Int]

    val result = runLog[IO](eab).unsafePerformIO()

    val expected = eab match {
      case \/-(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector(n.toString)
        )

      case -\/(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector(s"Error: $msg")
        )
    }

    Result.all(
      List(
        (result ==== eab).log("result ==== eab failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

}
