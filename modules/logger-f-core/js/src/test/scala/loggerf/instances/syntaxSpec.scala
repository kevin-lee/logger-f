package loggerf.instances

import cats.Monad
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.instances.future.fxCtor._
import loggerf.core.Log
import loggerf.core.syntax.all._
import loggerf.logger._
import loggerf.test_data.TestCases
import loggerf.testings.RandomGens
import munit.Assertions

import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
class syntaxSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test log(F[A])") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log(FxCtor[F].effectOf(debugMsg))(debug)
        _ <- log(FxCtor[F].effectOf(debugMsg))(ignoreA)
        _ <- log(FxCtor[F].effectOf(infoMsg))(info)
        _ <- log(FxCtor[F].effectOf(infoMsg))(ignoreA)
        _ <- log(FxCtor[F].effectOf(warnMsg))(warn)
        _ <- log(FxCtor[F].effectOf(warnMsg))(ignoreA)
        _ <- log(FxCtor[F].effectOf(errorMsg))(error)
        _ <- log(FxCtor[F].effectOf(errorMsg))(ignoreA)
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(F[A]) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log(FxCtor[F].effectOf(debugMsg))(debug(debugThrowable))
        _ <- log(FxCtor[F].effectOf(infoMsg))(info(infoThrowable))
        _ <- log(FxCtor[F].effectOf(warnMsg))(warn(warnThrowable))
        _ <- log(FxCtor[F].effectOf(errorMsg))(error(errorThrowable))
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$debugMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$infoMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$warnMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$errorMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(F[A]) with matching cases") {
    val debugMsg  = RandomGens.genAlphaNumericString(20)
    val infoMsg   = RandomGens.genAlphaNumericString(20)
    val warnMsg   = RandomGens.genAlphaNumericString(20)
    val errorMsg  = RandomGens.genAlphaNumericString(20)
    val testCases = RandomGens.genTestCases

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log(FxCtor[F].effectOf(testCases)) {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 debug(s"$debugMsg / id=${id.toString}, name=$name, not enabled")
             }
        _ <- log(FxCtor[F].effectOf(testCases)) {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 info(s"$infoMsg / id=${id.toString}, name=$name, not enabled")
             }
        _ <- log(FxCtor[F].effectOf(testCases)) {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 warn(s"$warnMsg / id=${id.toString}, name=$name, not enabled")
             }
        _ <- log(FxCtor[F].effectOf(testCases)) {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 error(s"$errorMsg / id=${id.toString}, name=$name, not enabled")
             }
      } yield ())

    val expected =
      if (testCases.enabled)
        LoggerForTesting()
      else
        LoggerForTesting(
          debugMessages = Vector(s"$debugMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
          infoMessages = Vector(s"$infoMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
          warnMessages = Vector(s"$warnMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
          errorMessages = Vector(s"$errorMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
        )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(F[A])") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      log_(FxCtor[F].effectOf(debugMsg))(debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(debugMsg))(ignoreA) }
        .flatMap { _ => log_(FxCtor[F].effectOf(infoMsg))(info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(infoMsg))(ignoreA) }
        .flatMap { _ => log_(FxCtor[F].effectOf(warnMsg))(warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(warnMsg))(ignoreA) }
        .flatMap { _ => log_(FxCtor[F].effectOf(errorMsg))(error) }
        .flatMap { _ => log_(FxCtor[F].effectOf(errorMsg))(ignoreA) }

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(F[A]) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- log_(FxCtor[F].effectOf(debugMsg))(debug(debugThrowable))
        _ <- log_(FxCtor[F].effectOf(infoMsg))(info(infoThrowable))
        _ <- log_(FxCtor[F].effectOf(warnMsg))(warn(warnThrowable))
        _ <- log_(FxCtor[F].effectOf(errorMsg))(error(errorThrowable))
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$debugMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$infoMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$warnMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$errorMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log_(F[A]) with matching cases") {
    val debugMsg  = RandomGens.genAlphaNumericString(20)
    val infoMsg   = RandomGens.genAlphaNumericString(20)
    val warnMsg   = RandomGens.genAlphaNumericString(20)
    val errorMsg  = RandomGens.genAlphaNumericString(20)
    val testCases = RandomGens.genTestCases

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] = {
      val fa = FxCtor[F].effectOf(testCases)
      log_(fa) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          debug(s"$debugMsg / id=${id.toString}, name=$name, not enabled")
      }
        .flatMap { _ =>
          log_(fa) {
            case TestCases(_, _, true) =>
              ignore
            case TestCases(id, name, false) =>
              info(s"$infoMsg / id=${id.toString}, name=$name, not enabled")
          }
        }
        .flatMap { _ =>
          log_(fa) {
            case TestCases(_, _, true) =>
              ignore
            case TestCases(id, name, false) =>
              warn(s"$warnMsg / id=${id.toString}, name=$name, not enabled")
          }
        }
        .flatMap { _ =>
          log_(fa) {
            case TestCases(_, _, true) =>
              ignore
            case TestCases(id, name, false) =>
              error(s"$errorMsg / id=${id.toString}, name=$name, not enabled")
          }
        }
    }

    val expected =
      if (testCases.enabled)
        LoggerForTesting()
      else
        LoggerForTesting(
          debugMessages = Vector(s"$debugMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
          infoMessages = Vector(s"$infoMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
          warnMessages = Vector(s"$warnMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
          errorMessages = Vector(s"$errorMsg / id=${testCases.id.toString}, name=${testCases.name}, not enabled"),
        )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test log(String)") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(String, String, String, String)] =
      for {
        msg1 <- logS(debugMsg)(debug)
        msg2 <- logS(infoMsg)(info)
        msg3 <- logS(warnMsg)(warn)
        msg4 <- logS(errorMsg)(error)
      } yield (msg1, msg2, msg3, msg4)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    val expected = (debugMsg, infoMsg, warnMsg, errorMsg)

    import loggerf.instances.future.logFuture
    runLog[Future].map { actual =>
      Assertions.assertEquals(actual, expected)
      Assertions.assertEquals(logger, expectedLogger)
    }
  }

  test("test log(String) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(String, String, String, String)] =
      for {
        msg1 <- logS(debugMsg)(debug(debugThrowable))
        msg2 <- logS(infoMsg)(info(infoThrowable))
        msg3 <- logS(warnMsg)(warn(warnThrowable))
        msg4 <- logS(errorMsg)(error(errorThrowable))
      } yield (msg1, msg2, msg3, msg4)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(s"$debugMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$infoMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$warnMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$errorMsg\n${errorThrowable.toString}"),
    )

    val expected = (debugMsg, infoMsg, warnMsg, errorMsg)

    import loggerf.instances.future.logFuture
    runLog[Future].map { actual =>
      Assertions.assertEquals(actual, expected)
      Assertions.assertEquals(logger, expectedLogger)
    }
  }

  test("test log_(String)") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(Unit, Unit, Unit, Unit)] =
      for {
        r1 <- logS_(debugMsg)(debug)
        r2 <- logS_(infoMsg)(info)
        r3 <- logS_(warnMsg)(warn)
        r4 <- logS_(errorMsg)(error)
      } yield (r1, r2, r3, r4)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    val expected = ((), (), (), ())
    import loggerf.instances.future.logFuture
    runLog[Future].map { actual =>
      Assertions.assertEquals(actual, expected)
      Assertions.assertEquals(logger, expectedLogger)
    }
  }

  test("test log_(String) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(Unit, Unit, Unit, Unit)] =
      for {
        r1 <- logS_(debugMsg)(debug(debugThrowable))
        r2 <- logS_(infoMsg)(info(infoThrowable))
        r3 <- logS_(warnMsg)(warn(warnThrowable))
        r4 <- logS_(errorMsg)(error(errorThrowable))
      } yield (r1, r2, r3, r4)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(s"$debugMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$infoMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$warnMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$errorMsg\n${errorThrowable.toString}"),
    )

    val expected = ((), (), (), ())
    import loggerf.instances.future.logFuture
    runLog[Future].map { actual =>
      Assertions.assertEquals(actual, expected)
      Assertions.assertEquals(logger, expectedLogger)
    }
  }

  test("test log(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]]) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]]) with Throwable - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]]) with Throwable - None") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(FxCtor[F].effectOf(oa))(ignore, debug)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, info)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, warn)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, error)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(FxCtor[F].effectOf(oa))(ignore, debug)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, info)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, warn)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, error)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), error) }

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]]) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), error) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]] with Throwable) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable)) }

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]] with Throwable) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable)) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(ignore, debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, error) }

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(ignore, debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, error) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    import loggerf.instances.future.logFuture
    runLog[Future](logMsg.some).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future](none).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(FxCtor[F].effectOf(eab))(error, b => debug(b.toString))
      _ <- log(FxCtor[F].effectOf(eab))(error, b => info(b.toString))
      _ <- log(FxCtor[F].effectOf(eab))(error, b => warn(b.toString))
      _ <- log(FxCtor[F].effectOf(eab))(error, b => error(b.toString))
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

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Either[A, B]]) with Throwable") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[Throwable, Int]): F[Either[Throwable, Unit]] = for {
      _ <- log(FxCtor[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => debug(debugThrowable)(b.toString),
           )
      _ <- log(FxCtor[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => info(infoThrowable)(b.toString),
           )
      _ <- log(FxCtor[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => warn(warnThrowable)(b.toString),
           )
      _ <- log(FxCtor[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => error(errorThrowable)(b.toString),
           )
    } yield ().asRight[Throwable]

    val eab = if (isRight) rightInt.asRight[Throwable] else errorThrowable.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(s"${n.toString}\n${debugThrowable.toString}"),
          infoMessages = Vector(s"${n.toString}\n${infoThrowable.toString}"),
          warnMessages = Vector(s"${n.toString}\n${warnThrowable.toString}"),
          errorMessages = Vector(s"${n.toString}\n${errorThrowable.toString}"),
        )

      case Left(err) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(s"$leftString\n${err.getMessage}\n${err.toString}"),
        )
    }

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(FxCtor[F].effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- log(FxCtor[F].effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- log(FxCtor[F].effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- log(FxCtor[F].effectOf(eab))(_ => ignore, b => error(b.toString))
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

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(FxCtor[F].effectOf(eab))(error, _ => ignore)
      _ <- log(FxCtor[F].effectOf(eab))(error, _ => ignore)
      _ <- log(FxCtor[F].effectOf(eab))(error, _ => ignore)
      _ <- log(FxCtor[F].effectOf(eab))(error, _ => ignore)
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

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Unit] =
      log_(FxCtor[F].effectOf(eab))(error, b => debug(b.toString))
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(error, b => info(b.toString)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(error, b => warn(b.toString)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(error, b => error(b.toString)) }

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

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Either[A, B]]) with Throwable") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[Throwable, Int]): F[Unit] =
      log_(FxCtor[F].effectOf(eab))(
        err => error(err)(s"$leftString\n${err.getMessage}"),
        b => debug(debugThrowable)(b.toString),
      )
        .flatMap { _ =>
          log_(FxCtor[F].effectOf(eab))(
            err => error(err)(s"$leftString\n${err.getMessage}"),
            b => info(infoThrowable)(b.toString),
          )
        }
        .flatMap { _ =>
          log_(FxCtor[F].effectOf(eab))(
            err => error(err)(s"$leftString\n${err.getMessage}"),
            b => warn(warnThrowable)(b.toString),
          )
        }
        .flatMap { _ =>
          log_(FxCtor[F].effectOf(eab))(
            err => error(err)(s"$leftString\n${err.getMessage}"),
            b => error(errorThrowable)(b.toString),
          )
        }

    val eab = if (isRight) rightInt.asRight[Throwable] else errorThrowable.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(s"${n.toString}\n${debugThrowable.toString}"),
          infoMessages = Vector(s"${n.toString}\n${infoThrowable.toString}"),
          warnMessages = Vector(s"${n.toString}\n${warnThrowable.toString}"),
          errorMessages = Vector(s"${n.toString}\n${errorThrowable.toString}"),
        )

      case Left(err) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(s"$leftString\n${err.getMessage}\n${err.toString}"),
        )
    }

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Unit] =
      log_(FxCtor[F].effectOf(eab))(_ => ignore, b => debug(b.toString))
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(_ => ignore, b => info(b.toString)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(_ => ignore, b => warn(b.toString)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(_ => ignore, b => error(b.toString)) }

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

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

  test("test log_(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Unit] =
      log_(FxCtor[F].effectOf(eab))(error, _ => ignore)
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(error, _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(error, _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(eab))(error, _ => ignore) }

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

    import loggerf.instances.future.logFuture
    runLog[Future](eab).map { _ =>
      Assertions.assertEquals(logger, expected)
    }
  }

}
