package loggerf.instances

import _root_.cats.Monad
import _root_.cats.data.{EitherT, OptionT}
import _root_.cats.syntax.all._
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
class LogExtensionSpec extends munit.FunSuite {

  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test String.logS with prefix") {
    val prefixString = RandomGens.genUnicodeString(1, 20)
    val debugMsg     = RandomGens.genUnicodeString(1, 20)
    val infoMsg      = RandomGens.genUnicodeString(1, 20)
    val warnMsg      = RandomGens.genUnicodeString(1, 20)
    val errorMsg     = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[Unit] =
      for {
        _ <- debugMsg.logS(debug(prefix(prefixString)))
        _ <- infoMsg.logS(info(prefix(prefixString)))
        _ <- warnMsg.logS(warn(prefix(prefixString)))
        _ <- errorMsg.logS(error(prefix(prefixString)))
      } yield ()

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + debugMsg),
      infoMessages = Vector(prefixString + infoMsg),
      warnMessages = Vector(prefixString + warnMsg),
      errorMessages = Vector(prefixString + errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test String.logS_ with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val debugMsg     = RandomGens.genUnicodeString(1, 20)
    val infoMsg      = RandomGens.genUnicodeString(1, 20)
    val warnMsg      = RandomGens.genUnicodeString(1, 20)
    val errorMsg     = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[Unit] =
      for {
        _ <- debugMsg.logS_(debug(prefix(prefixString)))
        _ <- infoMsg.logS_(info(prefix(prefixString)))
        _ <- warnMsg.logS_(warn(prefix(prefixString)))
        _ <- errorMsg.logS_(error(prefix(prefixString)))
      } yield ()

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + debugMsg),
      infoMessages = Vector(prefixString + infoMsg),
      warnMessages = Vector(prefixString + warnMsg),
      errorMessages = Vector(prefixString + errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[A].log with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val debugMsg     = RandomGens.genUnicodeString(1, 20)
    val infoMsg      = RandomGens.genUnicodeString(1, 20)
    val warnMsg      = RandomGens.genUnicodeString(1, 20)
    val errorMsg     = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      for {
        _ <- effectOf(debugMsg).log(debug(prefix(prefixString)))
        _ <- effectOf(infoMsg).log(info(prefix(prefixString)))
        _ <- effectOf(warnMsg).log(warn(prefix(prefixString)))
        _ <- effectOf(errorMsg).log(error(prefix(prefixString)))
      } yield ()

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + debugMsg),
      infoMessages = Vector(prefixString + infoMsg),
      warnMessages = Vector(prefixString + warnMsg),
      errorMessages = Vector(prefixString + errorMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + logMsg),
      infoMessages = Vector(prefixString + logMsg),
      warnMessages = Vector(prefixString + logMsg),
      errorMessages = Vector(prefixString + logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(ignore, message) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(ignore, debug(prefix(prefixString)))
        _ <- effectOf(oa).log(ignore, info(prefix(prefixString)))
        _ <- effectOf(oa).log(ignore, warn(prefix(prefixString)))
        _ <- effectOf(oa).log(ignore, error(prefix(prefixString)))
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + logMsg),
      infoMessages = Vector(prefixString + logMsg),
      warnMessages = Vector(prefixString + logMsg),
      errorMessages = Vector(prefixString + logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(ignore, message) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(ignore, debug(prefix(prefixString)))
        _ <- effectOf(oa).log(ignore, info(prefix(prefixString)))
        _ <- effectOf(oa).log(ignore, warn(prefix(prefixString)))
        _ <- effectOf(oa).log(ignore, error(prefix(prefixString)))
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(message, ignore) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Option[A]].log(message, ignore) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(error(prefix(prefixString)), b => debug(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(error(prefix(prefixString)), b => info(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(error(prefix(prefixString)), b => warn(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(error(prefix(prefixString)), b => error(prefix(prefixString))(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + n.toString),
          infoMessages = Vector(prefixString + n.toString),
          warnMessages = Vector(prefixString + n.toString),
          errorMessages = Vector(prefixString + n.toString),
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(prefixString + msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture
    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(ignore, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(_ => ignore, b => debug(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(_ => ignore, b => info(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(_ => ignore, b => warn(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(_ => ignore, b => error(prefix(prefixString))(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + n.toString),
          infoMessages = Vector(prefixString + n.toString),
          warnMessages = Vector(prefixString + n.toString),
          errorMessages = Vector(prefixString + n.toString),
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
    import loggerf.instances.future.logFuture
    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(ignoreA, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(ignoreA, b => debug(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(ignoreA, b => info(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(ignoreA, b => warn(prefix(prefixString))(b.toString))
      _ <- effectOf(eab).log(ignoreA, b => error(prefix(prefixString))(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + n.toString),
          infoMessages = Vector(prefixString + n.toString),
          warnMessages = Vector(prefixString + n.toString),
          errorMessages = Vector(prefixString + n.toString),
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
    import loggerf.instances.future.logFuture
    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(message, ignore) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(error(prefix(prefixString)), _ => ignore)
      _ <- effectOf(eab).log(error(prefix(prefixString)), _ => ignore)
      _ <- effectOf(eab).log(error(prefix(prefixString)), _ => ignore)
      _ <- effectOf(eab).log(error(prefix(prefixString)), _ => ignore)
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
          errorMessages = Vector.fill(4)(prefixString + msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture
    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test F[Either[A, B]].log(message, ignoreA) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- effectOf(eab).log(error(prefix(prefixString)), ignoreA)
      _ <- effectOf(eab).log(error(prefix(prefixString)), ignoreA)
      _ <- effectOf(eab).log(error(prefix(prefixString)), ignoreA)
      _ <- effectOf(eab).log(error(prefix(prefixString)), ignoreA)
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
          errorMessages = Vector.fill(4)(prefixString + msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture
    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + logMsg),
      infoMessages = Vector(prefixString + logMsg),
      warnMessages = Vector(prefixString + logMsg),
      errorMessages = Vector(prefixString + logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector(prefixString + ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(ignore, message) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(ignore, debug(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(ignore, info(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(ignore, warn(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(ignore, error(prefix(prefixString)))
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + logMsg),
      infoMessages = Vector(prefixString + logMsg),
      warnMessages = Vector(prefixString + logMsg),
      errorMessages = Vector(prefixString + logMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(ignore, message) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(ignore, debug(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(ignore, info(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(ignore, warn(prefix(prefixString)))
      _ <- OptionT(effectOf(oa)).log(ignore, error(prefix(prefixString)))
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(message, ignore) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test OptionT[F, A].log(message, ignore) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector(prefixString + ifEmptyMsg),
    )

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), b => debug(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), b => info(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), b => warn(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), b => error(prefix(prefixString))(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + n.toString),
          infoMessages = Vector(prefixString + n.toString),
          warnMessages = Vector(prefixString + n.toString),
          errorMessages = Vector(prefixString + n.toString),
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(prefixString + msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(ignore, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => debug(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => info(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => warn(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(_ => ignore, b => error(prefix(prefixString))(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + n.toString),
          infoMessages = Vector(prefixString + n.toString),
          warnMessages = Vector(prefixString + n.toString),
          errorMessages = Vector(prefixString + n.toString),
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
    import loggerf.instances.future.logFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(ignoreA, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => debug(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => info(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => warn(prefix(prefixString))(b.toString))
      _ <- EitherT(effectOf(eab)).log(ignoreA, b => error(prefix(prefixString))(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + n.toString),
          infoMessages = Vector(prefixString + n.toString),
          warnMessages = Vector(prefixString + n.toString),
          errorMessages = Vector(prefixString + n.toString),
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
    import loggerf.instances.future.logFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(message, ignore) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), _ => ignore)
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), _ => ignore)
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), _ => ignore)
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), _ => ignore)
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
          errorMessages = Vector(prefixString + msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test EitherT[F, A, B].log(message, ignoreA) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), ignoreA)
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), ignoreA)
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), ignoreA)
      _ <- EitherT(effectOf(eab)).log(error(prefix(prefixString)), ignoreA)
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
          errorMessages = Vector(prefixString + msg),
        )
    }

    import effectie.instances.future.fxCtor.fxCtorFuture
    import loggerf.instances.future.logFuture

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))

  }

}
