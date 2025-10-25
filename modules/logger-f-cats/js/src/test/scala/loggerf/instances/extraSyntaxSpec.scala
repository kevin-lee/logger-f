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
class extraSyntaxSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test logS(String)(level) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val debugMsg     = RandomGens.genUnicodeString(1, 20)
    val infoMsg      = RandomGens.genUnicodeString(1, 20)
    val warnMsg      = RandomGens.genUnicodeString(1, 20)
    val errorMsg     = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[Unit] =
      (for {
        _ <- logS(debugMsg)(debug(prefix(prefixString)))
        _ <- logS(infoMsg)(info(prefix(prefixString)))
        _ <- logS(warnMsg)(warn(prefix(prefixString)))
        _ <- logS(errorMsg)(error(prefix(prefixString)))
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + debugMsg),
      infoMessages = Vector(prefixString + infoMsg),
      warnMessages = Vector(prefixString + warnMsg),
      errorMessages = Vector(prefixString + errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test logS_(String)(level) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val debugMsg     = RandomGens.genUnicodeString(1, 20)
    val infoMsg      = RandomGens.genUnicodeString(1, 20)
    val warnMsg      = RandomGens.genUnicodeString(1, 20)
    val errorMsg     = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[Unit] =
      (for {
        _ <- logS_(debugMsg)(debug(prefix(prefixString)))
        _ <- logS_(infoMsg)(info(prefix(prefixString)))
        _ <- logS_(warnMsg)(warn(prefix(prefixString)))
        _ <- logS_(errorMsg)(error(prefix(prefixString)))
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(prefixString + debugMsg),
      infoMessages = Vector(prefixString + infoMsg),
      warnMessages = Vector(prefixString + warnMsg),
      errorMessages = Vector(prefixString + errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(F[A]) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val debugMsg     = RandomGens.genUnicodeString(1, 20)
    val infoMsg      = RandomGens.genUnicodeString(1, 20)
    val warnMsg      = RandomGens.genUnicodeString(1, 20)
    val errorMsg     = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log(effectOf(debugMsg))(debug(prefix(prefixString)))
        _ <- log(effectOf(infoMsg))(info(prefix(prefixString)))
        _ <- log(effectOf(warnMsg))(warn(prefix(prefixString)))
        _ <- log(effectOf(errorMsg))(error(prefix(prefixString)))
      } yield ())

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

  test("test log(F[Option[A]]) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
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

  test("test log(F[Option[A]]) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
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

  test("test log(F[Option[A]])(ignore, message) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(ignore, debug(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, info(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, warn(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, error(prefix(prefixString)))
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

  test("test log(F[Option[A]])(ignore, message) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(ignore, debug(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, info(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, warn(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, error(prefix(prefixString)))
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

  test("test log(F[Option[A]])(message, ignore) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
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

  test("test log(F[Option[A]])(message, ignore) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
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

  test("test log(F[Option[A]])(message, ignoreA) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
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

  test("test log(F[Option[A]])(message, ignoreA) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
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

  test("test log(F[Either[A, B]]) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error(prefix(prefixString)), b => debug(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(error(prefix(prefixString)), b => info(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(error(prefix(prefixString)), b => warn(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(error(prefix(prefixString)), b => error(prefix(prefixString))(b.toString))
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

  test("test log(F[Either[A, B]])(ignore, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(_ => ignore, b => debug(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => info(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => warn(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => error(prefix(prefixString))(b.toString))
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

  test("test log(F[Either[A, B]])(ignoreA, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(ignoreA, b => debug(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => info(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => warn(prefix(prefixString))(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => error(prefix(prefixString))(b.toString))
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

  test("test log(F[Either[A, B]])(message, ignore) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error(prefix(prefixString)), _ => ignore)
      _ <- log(effectOf(eab))(error(prefix(prefixString)), _ => ignore)
      _ <- log(effectOf(eab))(error(prefix(prefixString)), _ => ignore)
      _ <- log(effectOf(eab))(error(prefix(prefixString)), _ => ignore)
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

  test("test log(F[Either[A, B]])(message, ignoreA) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error(prefix(prefixString)), ignoreA)
      _ <- log(effectOf(eab))(error(prefix(prefixString)), ignoreA)
      _ <- log(effectOf(eab))(error(prefix(prefixString)), ignoreA)
      _ <- log(effectOf(eab))(error(prefix(prefixString)), ignoreA)
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

  test("test log(OptionT[F, A]) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
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

  test("test log(OptionT[F, A]) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
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

  test("test log(OptionT[F, A])(ignore, message) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, info(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, warn(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, error(prefix(prefixString)))
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

  test("test log(OptionT[F, A])(ignore, message) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, info(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, warn(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, error(prefix(prefixString)))
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

  test("test log(OptionT[F, A])(message, ignore) with prefix - Some case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val logMsg       = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
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

  test("test log(OptionT[F, A])(message, ignore) with prefix - None case") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val ifEmptyMsg   = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
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

  test("test log(EitherT[F, A, B]) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), b => debug(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), b => info(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), b => warn(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), b => error(prefix(prefixString))(b.toString))
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

  test("test log(EitherT[F, A, B])(ignore, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => debug(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => info(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => warn(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => error(prefix(prefixString))(b.toString))
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

  test("test log(EitherT[F, A, B])(ignoreA, message) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => debug(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => info(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => warn(prefix(prefixString))(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => error(prefix(prefixString))(b.toString))
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

  test("test log(EitherT[F, A, B])(message, ignore) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), _ => ignore)
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

  test("test log(EitherT[F, A, B])(message, ignoreA) with prefix") {
    val prefixString = RandomGens.genUnicodeString(5, 20)
    val rightInt     = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString   = RandomGens.genUnicodeString(1, 20)
    val isRight      = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error(prefix(prefixString)), ignoreA)
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

  // ////

}
