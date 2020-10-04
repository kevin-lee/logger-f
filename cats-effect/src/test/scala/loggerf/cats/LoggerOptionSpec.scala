package loggerf.cats

import cats._
import cats.effect._
import effectie.cats.Effectful._
import effectie.cats.EffectConstructor
import hedgehog._
import hedgehog.runner._
import loggerf.logger.LoggerForTesting

/**
 * @author Kevin Lee
 * @since 2020-04-13
 */
object LoggerOptionSpec extends Properties {
  override def tests: List[Test] = List(
    property("test LoggerOption.debugOption(F[Option[A]])", testLoggerOptionDebugOptionOFA)
  , property("test LoggerOption.infoOption(F[Option[A]])", testLoggerOptionInfoOptionOFA)
  , property("test LoggerOption.warnOption(F[Option[A]])", testLoggerOptionWarnOptionOFA)
  , property("test LoggerOption.errorOption(F[Option[A]])", testLoggerOptionErrorOptionOFA)
  )


  def testLoggerOptionDebugOptionOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
      LoggerOption[F].debugOption(effectOf(oa))(ifEmpty = emptyMsg, a => s"$logMsg - $a")

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

  def testLoggerOptionInfoOptionOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
     LoggerOption[F].infoOption(effectOf(oa))(ifEmpty = emptyMsg, a => s"$logMsg - $a")

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

  def testLoggerOptionWarnOptionOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
     LoggerOption[F].warnOption(effectOf(oa))(ifEmpty = emptyMsg, a => s"$logMsg - $a")

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

  def testLoggerOptionErrorOptionOFA: Property = for {
    oa <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).option.log("oa")
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("logMsg")
    emptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("emptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad](oa: Option[Int]): F[Option[Int]] =
     LoggerOption[F].errorOption(effectOf(oa))(ifEmpty = emptyMsg, a => s"$logMsg - $a")

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
