package loggerf.instances

import _root_.cats.data.{EitherT, OptionT}
import _root_.cats.syntax.either._
import _root_.cats.syntax.option._
import loggerf.core._
import loggerf.instances.future.logFuture
import loggerf.logger._
import loggerf.syntax.all._
import loggerf.testings.RandomGens
import munit.Assertions

import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
class futureSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test Log.log(F[A])") {
    val debugMsg = RandomGens.genUnicodeString(1, 20)
    val infoMsg  = RandomGens.genUnicodeString(1, 20)
    val warnMsg  = RandomGens.genUnicodeString(1, 20)
    val errorMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    def runLog: Future[Unit] =
      (for {
        _ <- Log[Future].log(Future(debugMsg))(debug)
        _ <- Log[Future].log(Future(infoMsg))(info)
        _ <- Log[Future].log(Future(warnMsg))(warn)
        _ <- Log[Future].log(Future(errorMsg))(error)
      } yield ())

    runLog.map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), debug)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), info)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), warn)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    runLog(logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), debug)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), info)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), warn)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    runLog(none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(ignore, debug)
        _ <- Log[Future].log(Future(oa))(ignore, info)
        _ <- Log[Future].log(Future(oa))(ignore, warn)
        _ <- Log[Future].log(Future(oa))(ignore, error)
      } yield ().some)

    runLog(logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]])(ignore, message) - None case") {
    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(ignore, debug)
        _ <- Log[Future].log(Future(oa))(ignore, info)
        _ <- Log[Future].log(Future(oa))(ignore, warn)
        _ <- Log[Future].log(Future(oa))(ignore, error)
      } yield ().some)

    runLog(none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    runLog(logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    runLog(none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test log(OptionT[Future, A]]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      (for {
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), debug)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), info)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), warn)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), error)
      } yield ())

    runLog(logMsg.some).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(OptionT[Future, A]]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(1)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      (for {
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), debug)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), info)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), warn)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), error)
      } yield ())

    runLog(none).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(OptionT[Future, A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      for {
        _ <- log(OptionT(Future(oa)))(ignore, debug)
        _ <- log(OptionT(Future(oa)))(ignore, info)
        _ <- log(OptionT(Future(oa)))(ignore, warn)
        _ <- log(OptionT(Future(oa)))(ignore, error)
      } yield ()

    runLog(logMsg.some).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(OptionT[Future, A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      for {
        _ <- log(OptionT(Future(oa)))(ignore, debug)
        _ <- log(OptionT(Future(oa)))(ignore, info)
        _ <- log(OptionT(Future(oa)))(ignore, warn)
        _ <- log(OptionT(Future(oa)))(ignore, error)
      } yield ()

    runLog(none).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(OptionT[Future, A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      for {
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
      } yield ()

    runLog(logMsg.some).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(OptionT[Future, A]])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(1)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      for {
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
      } yield ()

    runLog(none).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): Future[Either[String, Unit]] = for {
      _ <- Log[Future].log(Future(eab))(error, b => debug(b.toString))
      _ <- Log[Future].log(Future(eab))(error, b => info(b.toString))
      _ <- Log[Future].log(Future(eab))(error, b => warn(b.toString))
      _ <- Log[Future].log(Future(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    runLog(eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )
    }

    def runLog(eab: Either[String, Int]): Future[Either[String, Unit]] = for {
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => debug(b.toString))
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => info(b.toString))
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => warn(b.toString))
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    runLog(eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): Future[Either[String, Unit]] = for {
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
    } yield ().asRight[String]

    runLog(eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(EitherT[Future, A, B])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(1)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log(EitherT(Future(eab)))(error, b => debug(b.toString))
      _ <- log(EitherT(Future(eab)))(error, b => info(b.toString))
      _ <- log(EitherT(Future(eab)))(error, b => warn(b.toString))
      _ <- log(EitherT(Future(eab)))(error, b => error(b.toString))
    } yield ()

    runLog(eab).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(EitherT[Future, A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )
    }

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => info(b.toString))
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => error(b.toString))
    } yield ()

    runLog(eab).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(EitherT[Future, A, B]])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(1)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
    } yield ()

    runLog(eab).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[A])") {
    val debugMsg = RandomGens.genUnicodeString(1, 20)
    val infoMsg  = RandomGens.genUnicodeString(1, 20)
    val warnMsg  = RandomGens.genUnicodeString(1, 20)
    val errorMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    def runLog: Future[Unit] =
      Log[Future]
        .log_(Future(debugMsg))(debug)
        .flatMap { _ => Log[Future].log_(Future(infoMsg))(info) }
        .flatMap { _ => Log[Future].log_(Future(warnMsg))(warn) }
        .flatMap { _ => Log[Future].log_(Future(errorMsg))(error) }

    runLog.map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log_(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), error) }

    runLog(logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), error) }

    runLog(none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(ignore, debug)
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, info) }
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, warn) }
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, error) }

    runLog(logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(ignore, debug)
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, info) }
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, warn) }
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, error) }

    runLog(none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }

    runLog(logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }

    runLog(none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(OptionT[Future, A]]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(error(ifEmptyMsg), debug)
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), info) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), warn) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), error) }

    runLog(logMsg.some).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(OptionT[Future, A]]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(1)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(error(ifEmptyMsg), debug)
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), info) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), warn) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), error) }

    runLog(none).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(OptionT[Future, A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(ignore, debug)
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, info) }
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, warn) }
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, error) }

    runLog(logMsg.some).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(OptionT[Future, A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(ignore, debug)
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, info) }
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, warn) }
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, error) }

    runLog(none).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(OptionT[Future, A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }

    runLog(logMsg.some).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(OptionT[Future, A]])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(1)(ifEmptyMsg),
    )

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }

    runLog(none).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): Future[Unit] =
      Log[Future]
        .log_(Future(eab))(error, b => debug(b.toString))
        .flatMap { _ => Log[Future].log_(Future(eab))(error, b => info(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, b => warn(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, b => error(b.toString)) }

    runLog(eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )
    }

    def runLog(eab: Either[String, Int]): Future[Unit] =
      Log[Future]
        .log_(Future(eab))(_ => ignore, b => debug(b.toString))
        .flatMap { _ => Log[Future].log_(Future(eab))(_ => ignore, b => info(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(_ => ignore, b => warn(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(_ => ignore, b => error(b.toString)) }

    runLog(eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): Future[Unit] =
      Log[Future]
        .log_(Future(eab))(error, _ => ignore)
        .flatMap { _ => Log[Future].log_(Future(eab))(error, _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, _ => ignore) }

    runLog(eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  ///

  test("test log_(EitherT[Future, A, B])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(1)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log_(EitherT(Future(eab)))(error, b => debug(b.toString))
      _ <- log_(EitherT(Future(eab)))(error, b => info(b.toString))
      _ <- log_(EitherT(Future(eab)))(error, b => warn(b.toString))
      _ <- log_(EitherT(Future(eab)))(error, b => error(b.toString))
    } yield ()

    runLog(eab).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(EitherT[Future, A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString),
        )

      case Left(msg @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )
    }

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => info(b.toString))
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => error(b.toString))
    } yield ()

    runLog(eab).value.map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(EitherT[Future, A, B]])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(1)(msg),
        )
    }

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
    } yield ()

    runLog(eab).value.map(_ => Assertions.assertEquals(logger, expected))
  }

}
