package loggerf.cats

import cats._
import cats.data.{EitherT, OptionT}
import cats.implicits._
import cats.effect._

import effectie.Effectful._
import effectie.cats.EffectConstructor

import hedgehog._
import hedgehog.runner._

import loggerf.LoggerForTesting
import loggerf.cats.Log.LeveledMessage._

/**
 * @author Kevin Lee
 * @since 2020-04-12
 */
object LogSpec extends Properties {
  override def tests: List[Test] = List(
    property("test Log.log(F[A])", testLogFA)
  , property("test Log.log(F[Option[A]])", testLogFOptionA)
  , property("test Log.log(F[Either[A, B]])", testLogFEitherAB)
  , property("test Log.log(OptionT[F, A])", testLogOptionTFA)
  , property("test Log.log(EitherT[F, A, B])", testLogEitherTFAB)
  )

  def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad]: F[Unit] = (for {
      _ <- Log[F].log(effectOf(debugMsg))(debug)
      _ <- Log[F].log(effectOf(infoMsg))(info)
      _ <- Log[F].log(effectOf(warnMsg))(warn)
      _ <- Log[F].log(effectOf(errorMsg))(error)
    } yield ())

    runLog[IO].unsafeRunSync()

    val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg)
      , infoMessages = Vector(infoMsg)
      , warnMessages = Vector(warnMsg)
      , errorMessages = Vector(errorMsg)
      )

    logger ==== expected
  }

  def testLogFOptionA: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), debug)
      _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), info)
      _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), warn)
      _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), error)
    } yield ().some)

    runLog[IO](logMsg).unsafeRunSync()

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector (logMsg)
        , infoMessages = Vector (logMsg)
        , warnMessages = Vector (logMsg)
        , errorMessages = Vector (logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector.fill(4)(ifEmptyMsg)
        )
    }

    logger ==== expected
  }

  def testLogFEitherAB: Property = for {
    rightInt <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRIght <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(effectOf(eab))(error, b => debug(b.toString))
      _ <- Log[F].log(effectOf(eab))(error, b => info(b.toString))
      _ <- Log[F].log(effectOf(eab))(error, b => warn(b.toString))
      _ <- Log[F].log(effectOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRIght) rightInt.asRight[String] else leftString.asLeft[Int]

    runLog[IO](eab).unsafeRunSync()

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector (n.toString)
        , infoMessages = Vector (n.toString)
        , warnMessages = Vector (n.toString)
        , errorMessages = Vector (n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector.fill(4)(msg)
        )
    }

    logger ==== expected
  }

  def testLogOptionTFA: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- Log[F].log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- Log[F].log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- Log[F].log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- Log[F].log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    runLog[IO](logMsg).unsafeRunSync()

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector (logMsg)
        , infoMessages = Vector (logMsg)
        , warnMessages = Vector (logMsg)
        , errorMessages = Vector (logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector(ifEmptyMsg)
        )
    }

    logger ==== expected
  }

  def testLogEitherTFAB: Property = for {
    rightInt <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRIght <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- Log[F].log(EitherT(effectOf(eab)))(error, b => debug(b.toString))
      _ <- Log[F].log(EitherT(effectOf(eab)))(error, b => info(b.toString))
      _ <- Log[F].log(EitherT(effectOf(eab)))(error, b => warn(b.toString))
      _ <- Log[F].log(EitherT(effectOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRIght) rightInt.asRight[String] else leftString.asLeft[Int]

    runLog[IO](eab).unsafeRunSync()

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector (n.toString)
        , infoMessages = Vector (n.toString)
        , warnMessages = Vector (n.toString)
        , errorMessages = Vector (n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty
        , infoMessages = Vector.empty
        , warnMessages = Vector.empty
        , errorMessages = Vector(msg)
        )
    }

    logger ==== expected
  }

}
