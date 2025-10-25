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
class loggingSyntaxSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test log(F[A])") {
    val debugMsg = RandomGens.genUnicodeString(1, 20)
    val infoMsg  = RandomGens.genUnicodeString(1, 20)
    val warnMsg  = RandomGens.genUnicodeString(1, 20)
    val errorMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log(effectOf(debugMsg))(debug)
        _ <- log(effectOf(infoMsg))(info)
        _ <- log(effectOf(warnMsg))(warn)
        _ <- log(effectOf(errorMsg))(error)
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

  test("test log(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), error)
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

  test("test log(F[Option[A]]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), error)
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

  test("test log(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(ignore, debug)
        _ <- log(effectOf(oa))(ignore, info)
        _ <- log(effectOf(oa))(ignore, warn)
        _ <- log(effectOf(oa))(ignore, error)
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

  test("test log(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(ignore, debug)
        _ <- log(effectOf(oa))(ignore, info)
        _ <- log(effectOf(oa))(ignore, warn)
        _ <- log(effectOf(oa))(ignore, error)
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

  test("test log(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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

  test("test log(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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

  test("test log(F[Option[A]])(message, ignoreA) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
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

  test("test log(F[Option[A]])(message, ignoreA) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
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

  test("test log(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, b => debug(b.toString))
      _ <- log(effectOf(eab))(error, b => info(b.toString))
      _ <- log(effectOf(eab))(error, b => warn(b.toString))
      _ <- log(effectOf(eab))(error, b => error(b.toString))
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

  test("test log(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => error(b.toString))
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

  test("test log(F[Either[A, B]])(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(ignoreA, b => debug(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => info(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => warn(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => error(b.toString))
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

  test("test log(F[Either[A, B]])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
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

  test("test log(F[Either[A, B]])(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
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

  test("test log(OptionT[F, A]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
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

  test("test log(OptionT[F, A]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
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

  test("test log(OptionT[F, A])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log(OptionT(effectOf(oa)))(ignore, info)
      _ <- log(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log(OptionT(effectOf(oa)))(ignore, error)
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

  test("test log(OptionT[F, A])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log(OptionT(effectOf(oa)))(ignore, info)
      _ <- log(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log(OptionT(effectOf(oa)))(ignore, error)
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

  test("test log(OptionT[F, A])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
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

  test("test log(OptionT[F, A])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
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

  test("test log(EitherT[F, A, B])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => error(b.toString))
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

  test("test log(EitherT[F, A, B])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => error(b.toString))
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

  test("test log(EitherT[F, A, B])(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => error(b.toString))
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

  test("test log(EitherT[F, A, B])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
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

  test("test log(EitherT[F, A, B])(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
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
  // ////

  test("test log_(F[A])") {
    val debugMsg = RandomGens.genUnicodeString(1, 20)
    val infoMsg  = RandomGens.genUnicodeString(1, 20)
    val warnMsg  = RandomGens.genUnicodeString(1, 20)
    val errorMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log_(effectOf(debugMsg))(debug)
        _ <- log_(effectOf(infoMsg))(info)
        _ <- log_(effectOf(warnMsg))(warn)
        _ <- log_(effectOf(errorMsg))(error)
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

  test("test log_(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), error)
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

  test("test log_(F[Option[A]]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), error)
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

  test("test log_(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log_(effectOf(oa))(ignore, debug)
        _ <- log_(effectOf(oa))(ignore, info)
        _ <- log_(effectOf(oa))(ignore, warn)
        _ <- log_(effectOf(oa))(ignore, error)
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

  test("test log_(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log_(effectOf(oa))(ignore, debug)
        _ <- log_(effectOf(oa))(ignore, info)
        _ <- log_(effectOf(oa))(ignore, warn)
        _ <- log_(effectOf(oa))(ignore, error)
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

  test("test log_(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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

  test("test log_(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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

  test("test log_(F[Option[A]])(message, ignoreA) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
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

  test("test log_(F[Option[A]])(message, ignoreA) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
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

  test("test log_(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log_(effectOf(eab))(error, b => debug(b.toString))
      _ <- log_(effectOf(eab))(error, b => info(b.toString))
      _ <- log_(effectOf(eab))(error, b => warn(b.toString))
      _ <- log_(effectOf(eab))(error, b => error(b.toString))
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

  test("test log_(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log_(effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- log_(effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- log_(effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- log_(effectOf(eab))(_ => ignore, b => error(b.toString))
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

  test("test log_(F[Either[A, B]])(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log_(effectOf(eab))(ignoreA, b => debug(b.toString))
      _ <- log_(effectOf(eab))(ignoreA, b => info(b.toString))
      _ <- log_(effectOf(eab))(ignoreA, b => warn(b.toString))
      _ <- log_(effectOf(eab))(ignoreA, b => error(b.toString))
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

  test("test log_(F[Either[A, B]])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log_(effectOf(eab))(error, _ => ignore)
      _ <- log_(effectOf(eab))(error, _ => ignore)
      _ <- log_(effectOf(eab))(error, _ => ignore)
      _ <- log_(effectOf(eab))(error, _ => ignore)
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

  test("test log_(F[Either[A, B]])(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log_(effectOf(eab))(error, ignoreA)
      _ <- log_(effectOf(eab))(error, ignoreA)
      _ <- log_(effectOf(eab))(error, ignoreA)
      _ <- log_(effectOf(eab))(error, ignoreA)
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

  test("test log_(OptionT[F, A]) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
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

  test("test log_(OptionT[F, A]) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
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

  test("test log_(OptionT[F, A])(ignore, message) - Some case") {
    val logMsg = RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log_(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log_(OptionT(effectOf(oa)))(ignore, info)
      _ <- log_(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log_(OptionT(effectOf(oa)))(ignore, error)
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

  test("test log_(OptionT[F, A])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log_(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log_(OptionT(effectOf(oa)))(ignore, info)
      _ <- log_(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log_(OptionT(effectOf(oa)))(ignore, error)
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

  test("test log_(OptionT[F, A])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genUnicodeString(1, 20)
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
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

  test("test log_(OptionT[F, A])(message, ignore) - None case") {
    val ifEmptyMsg = "[Empty] " + RandomGens.genUnicodeString(1, 20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
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

  test("test log_(EitherT[F, A, B])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log_(EitherT(effectOf(eab)))(error, b => debug(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(error, b => info(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(error, b => warn(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(error, b => error(b.toString))
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

  test("test log_(EitherT[F, A, B])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log_(EitherT(effectOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(_ => ignore, b => error(b.toString))
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

  test("test log_(EitherT[F, A, B])(ignoreA, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log_(EitherT(effectOf(eab)))(ignoreA, b => debug(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(ignoreA, b => info(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(ignoreA, b => warn(b.toString))
      _ <- log_(EitherT(effectOf(eab)))(ignoreA, b => error(b.toString))
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

  test("test log_(EitherT[F, A, B])(message, ignore)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log_(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log_(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log_(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log_(EitherT(effectOf(eab)))(error, _ => ignore)
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

  test("test log_(EitherT[F, A, B])(message, ignoreA)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genUnicodeString(1, 20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log_(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log_(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log_(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log_(EitherT(effectOf(eab)))(error, ignoreA)
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
  // ////

}
