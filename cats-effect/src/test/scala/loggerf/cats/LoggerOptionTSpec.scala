package loggerf.cats

import cats._
import cats.data.OptionT
import cats.effect._

import effectie.Effectful._
import effectie.cats.EffectConstructor

import hedgehog._
import hedgehog.runner._

import loggerf.LoggerForTesting

/**
 * @author Kevin Lee
 * @since 2020-04-13
 */
object LoggerOptionTSpec extends Properties {
  override def tests: List[Test] = List(
    property("test LoggerOptionT.debugOptionT(OptionT[F, A])", testLoggerOptionTDebugOptionTOFA)
  , property("test LoggerOptionT.infoOptionT(OptionT[F, A])", testLoggerOptionTInfoOptionTOFA)
  , property("test LoggerOptionT.warnOptionT(OptionT[F, A])", testLoggerOptionTWarnOptionTOFA)
  , property("test LoggerOptionT.errorOptionT(OptionT[F, A])", testLoggerOptionTErrorOptionTOFA)
  )


  def testLoggerOptionTDebugOptionTOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
      LoggerOptionT[F].debugOptionT(OptionT(effectOf(oa)))(ifEmpty = emptyMsg, a => s"$logMsg - $a").value

    val result = runLog[IO](oa).unsafeRunSync()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = oa match {
      case Some(n) =>
        LoggerForTesting(
          debugMessages = Vector(s"$logMsg - $n")
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector(emptyMsg)
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )
    }

    Result.all(
      List(
        (result ==== oa).log("result ==== oa failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testLoggerOptionTInfoOptionTOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
     LoggerOptionT[F].infoOptionT(OptionT(effectOf(oa)))(ifEmpty = emptyMsg, a => s"$logMsg - $a").value

    val result = runLog[IO](oa).unsafeRunSync()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = oa match {
      case Some(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector(s"$logMsg - $n")
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector(emptyMsg)
        , warnMessages = Vector.empty
        , errorMessages = Vector.empty
        )
    }

    Result.all(
      List(
        (result ==== oa).log("result ==== oa failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testLoggerOptionTWarnOptionTOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
     LoggerOptionT[F].warnOptionT(OptionT(effectOf(oa)))(ifEmpty = emptyMsg, a => s"$logMsg - $a").value

    val result = runLog[IO](oa).unsafeRunSync()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = oa match {
      case Some(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector(s"$logMsg - $n")
        , errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector(emptyMsg)
        , errorMessages = Vector.empty
        )
    }

    Result.all(
      List(
        (result ==== oa).log("result ==== oa failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testLoggerOptionTErrorOptionTOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
     LoggerOptionT[F].errorOptionT(OptionT(effectOf(oa)))(ifEmpty = emptyMsg, a => s"$logMsg - $a").value

    val result = runLog[IO](oa).unsafeRunSync()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = oa match {
      case Some(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector(s"$logMsg - $n")
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector(emptyMsg)
        )
    }

    Result.all(
      List(
        (result ==== oa).log("result ==== oa failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

}
