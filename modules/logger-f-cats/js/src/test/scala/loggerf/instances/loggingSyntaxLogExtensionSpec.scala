package loggerf.instances

import _root_.cats.Monad
import _root_.cats.data.{EitherT, OptionT}
import _root_.cats.syntax.either._
import _root_.cats.syntax.flatMap._
import _root_.cats.syntax.functor._
import _root_.cats.syntax.option._
import effectie.core.FxCtor
import effectie.syntax.all._
import loggerf.core.Log
import loggerf.logger._
import loggerf.syntax.all._
import loggerf.testings.RandomGens
import munit.Assertions

import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
class loggingSyntaxLogExtensionSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test F[A].log") {
    val debugMsg = RandomGens.genUnicodeString(1, 20)
    val infoMsg  = RandomGens.genUnicodeString(1, 20)
    val warnMsg  = RandomGens.genUnicodeString(1, 20)
    val errorMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- effectOf(debugMsg).log(debug)
        _ <- effectOf(infoMsg).log(info)
        _ <- effectOf(warnMsg).log(warn)
        _ <- effectOf(errorMsg).log(error)
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- effectOf(oa).log(error(ifEmptyMsg), debug)
        _ <- effectOf(oa).log(error(ifEmptyMsg), info)
        _ <- effectOf(oa).log(error(ifEmptyMsg), warn)
        _ <- effectOf(oa).log(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- effectOf(oa).log(error(ifEmptyMsg), debug)
        _ <- effectOf(oa).log(error(ifEmptyMsg), info)
        _ <- effectOf(oa).log(error(ifEmptyMsg), warn)
        _ <- effectOf(oa).log(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(ignore, debug)
        _ <- effectOf(oa).log(ignore, info)
        _ <- effectOf(oa).log(ignore, warn)
        _ <- effectOf(oa).log(ignore, error)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(ignore, debug)
        _ <- effectOf(oa).log(ignore, info)
        _ <- effectOf(oa).log(ignore, warn)
        _ <- effectOf(oa).log(ignore, error)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(error, b => debug(b.toString))
      _ <- effectOf(eab).log(error, b => info(b.toString))
      _ <- effectOf(eab).log(error, b => warn(b.toString))
      _ <- effectOf(eab).log(error, b => error(b.toString))
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(_ => ignore, b => debug(b.toString))
      _ <- effectOf(eab).log(_ => ignore, b => info(b.toString))
      _ <- effectOf(eab).log(_ => ignore, b => warn(b.toString))
      _ <- effectOf(eab).log(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(ignoreA, b => debug(b.toString))
      _ <- effectOf(eab).log(ignoreA, b => info(b.toString))
      _ <- effectOf(eab).log(ignoreA, b => warn(b.toString))
      _ <- effectOf(eab).log(ignoreA, b => error(b.toString))
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(error, _ => ignore)
      _ <- effectOf(eab).log(error, _ => ignore)
      _ <- effectOf(eab).log(error, _ => ignore)
      _ <- effectOf(eab).log(error, _ => ignore)
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(error, ignoreA)
      _ <- effectOf(eab).log(error, ignoreA)
      _ <- effectOf(eab).log(error, ignoreA)
      _ <- effectOf(eab).log(error, ignoreA)
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), debug)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), info)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), warn)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log - None case") {
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), debug)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), info)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), warn)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(ignore, debug)
      _ <- OptionT(effectOf(oa)).log(ignore, info)
      _ <- OptionT(effectOf(oa)).log(ignore, warn)
      _ <- OptionT(effectOf(oa)).log(ignore, error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(ignore, debug)
      _ <- OptionT(effectOf(oa)).log(ignore, info)
      _ <- OptionT(effectOf(oa)).log(ignore, warn)
      _ <- OptionT(effectOf(oa)).log(ignore, error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(error, b => debug(b.toString))
      _ <- EitherT(effectOf(eab)).log(error, b => info(b.toString))
      _ <- EitherT(effectOf(eab)).log(error, b => warn(b.toString))
      _ <- EitherT(effectOf(eab)).log(error, b => error(b.toString))
    } yield ()).value

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
          errorMessages = Vector(msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => debug(b.toString))
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => info(b.toString))
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => warn(b.toString))
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => error(b.toString))
    } yield ()).value

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => debug(b.toString))
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => info(b.toString))
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => warn(b.toString))
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => error(b.toString))
    } yield ()).value

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
      _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
      _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
      _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
    } yield ()).value

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
          errorMessages = Vector(msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test EitherT[F, A, B].log(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(error, ignoreA)
      _ <- EitherT(effectOf(eab)).log(error, ignoreA)
      _ <- EitherT(effectOf(eab)).log(error, ignoreA)
      _ <- EitherT(effectOf(eab)).log(error, ignoreA)
    } yield ()).value

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
          errorMessages = Vector(msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[A].log_") {
    val debugMsg = RandomGens.genUnicodeString(1, 20)
    val infoMsg  = RandomGens.genUnicodeString(1, 20)
    val warnMsg  = RandomGens.genUnicodeString(1, 20)
    val errorMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      for {
        _ <- effectOf(debugMsg).log_(debug)
        _ <- effectOf(infoMsg).log_(info)
        _ <- effectOf(warnMsg).log_(warn)
        _ <- effectOf(errorMsg).log_(error)
      } yield ()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log_ - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- effectOf(oa).log_(error(ifEmptyMsg), debug)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), info)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), warn)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log_ - None case") {
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- effectOf(oa).log_(error(ifEmptyMsg), debug)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), info)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), warn)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log_(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log_(ignore, debug)
        _ <- effectOf(oa).log_(ignore, info)
        _ <- effectOf(oa).log_(ignore, warn)
        _ <- effectOf(oa).log_(ignore, error)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log_(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log_(ignore, debug)
        _ <- effectOf(oa).log_(ignore, info)
        _ <- effectOf(oa).log_(ignore, warn)
        _ <- effectOf(oa).log_(ignore, error)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log_(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log_(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log_") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log_(error, b => debug(b.toString))
      _ <- effectOf(eab).log_(error, b => info(b.toString))
      _ <- effectOf(eab).log_(error, b => warn(b.toString))
      _ <- effectOf(eab).log_(error, b => error(b.toString))
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log_(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log_(_ => ignore, b => debug(b.toString))
      _ <- effectOf(eab).log_(_ => ignore, b => info(b.toString))
      _ <- effectOf(eab).log_(_ => ignore, b => warn(b.toString))
      _ <- effectOf(eab).log_(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log_(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log_(ignoreA, b => debug(b.toString))
      _ <- effectOf(eab).log_(ignoreA, b => info(b.toString))
      _ <- effectOf(eab).log_(ignoreA, b => warn(b.toString))
      _ <- effectOf(eab).log_(ignoreA, b => error(b.toString))
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log_(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log_(error, _ => ignore)
      _ <- effectOf(eab).log_(error, _ => ignore)
      _ <- effectOf(eab).log_(error, _ => ignore)
      _ <- effectOf(eab).log_(error, _ => ignore)
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log_(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log_(error, ignoreA)
      _ <- effectOf(eab).log_(error, ignoreA)
      _ <- effectOf(eab).log_(error, ignoreA)
      _ <- effectOf(eab).log_(error, ignoreA)
    } yield ().asRight[String]

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log_ - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), debug)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), info)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), warn)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log_ - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), debug)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), info)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), warn)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log_(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log_(ignore, debug)
      _ <- OptionT(effectOf(oa)).log_(ignore, info)
      _ <- OptionT(effectOf(oa)).log_(ignore, warn)
      _ <- OptionT(effectOf(oa)).log_(ignore, error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log_(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log_(ignore, debug)
      _ <- OptionT(effectOf(oa)).log_(ignore, info)
      _ <- OptionT(effectOf(oa)).log_(ignore, warn)
      _ <- OptionT(effectOf(oa)).log_(ignore, error)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log_(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log_(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector(ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log_") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log_(error, b => debug(b.toString))
      _ <- EitherT(effectOf(eab)).log_(error, b => info(b.toString))
      _ <- EitherT(effectOf(eab)).log_(error, b => warn(b.toString))
      _ <- EitherT(effectOf(eab)).log_(error, b => error(b.toString))
    } yield ()).value

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
          errorMessages = Vector(msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log_(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => debug(b.toString))
      _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => info(b.toString))
      _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => warn(b.toString))
      _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => error(b.toString))
    } yield ()).value

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log_(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log_(ignoreA, b => debug(b.toString))
      _ <- EitherT(effectOf(eab)).log_(ignoreA, b => info(b.toString))
      _ <- EitherT(effectOf(eab)).log_(ignoreA, b => warn(b.toString))
      _ <- EitherT(effectOf(eab)).log_(ignoreA, b => error(b.toString))
    } yield ()).value

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

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log_(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
      _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
      _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
      _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
    } yield ()).value

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
          errorMessages = Vector(msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log_(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
      _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
      _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
      _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
    } yield ()).value

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
          errorMessages = Vector(msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

}
