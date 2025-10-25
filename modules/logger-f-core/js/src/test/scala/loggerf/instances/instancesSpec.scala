package loggerf.instances

import cats.Monad
import cats.syntax.all._
import effectie.core._
import effectie.instances.future.fx._
import loggerf.core._
import loggerf.core.syntax.all._
import loggerf.instances.future.logFuture
import loggerf.logger._
import loggerf.test_data.TestCases
import loggerf.testings.RandomGens
import munit.Assertions

import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
class instancesSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test Log.log(F[A])") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(debug)
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(ignoreA)
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(info)
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(ignoreA)
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(warn)
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(ignoreA)
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(error)
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(ignoreA)
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[A] with Throwable)") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = RandomGens.genThrowable

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(debug)
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(debug(debugThrowable))
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(a => debug(debugThrowable)(s"DEBUG message: $a"))
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(ignoreA)
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(info)
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(info(infoThrowable))
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(a => info(infoThrowable)(s"INFO message: $a"))
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(ignoreA)
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(warn)
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(warn(warnThrowable))
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(a => warn(warnThrowable)(s"WARN message: $a"))
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(ignoreA)
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(error)
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(error(errorThrowable))
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(a => error(errorThrowable)(s"ERROR message: $a"))
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(ignoreA)
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(
        debugMsg,
        s"$debugMsg\n${debugThrowable.toString}",
        s"DEBUG message: $debugMsg\n${debugThrowable.toString}",
      ),
      infoMessages = Vector(
        infoMsg,
        s"$infoMsg\n${infoThrowable.toString}",
        s"INFO message: $infoMsg\n${infoThrowable.toString}",
      ),
      warnMessages = Vector(
        warnMsg,
        s"$warnMsg\n${warnThrowable.toString}",
        s"WARN message: $warnMsg\n${warnThrowable.toString}",
      ),
      errorMessages = Vector(
        errorMsg,
        s"$errorMsg\n${errorThrowable.toString}",
        s"ERROR message: $errorMsg\n${errorThrowable.toString}",
      ),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[A]) with matching cases") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val testCases = RandomGens.genTestCases

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] = {
      val fa1 = Log[F].log(Fx[F].effectOf(testCases)) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          debug(s"$debugMsg / id=${id.toString}, name=$name, not enabled")
      }
      val fa2 = Log[F].log(fa1) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          info(s"$infoMsg / id=${id.toString}, name=$name, not enabled")
      }
      val fa3 = Log[F].log(fa2) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          warn(s"$warnMsg / id=${id.toString}, name=$name, not enabled")
      }
      val fa4 = Log[F].log(fa3) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          error(s"$errorMsg / id=${id.toString}, name=$name, not enabled")
      }
      fa4 *> Fx[F].unitOf

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

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log_(F[A])") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(debugMsg))(debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(debugMsg))(ignoreA) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(infoMsg))(info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(infoMsg))(ignoreA) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(warnMsg))(warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(warnMsg))(ignoreA) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(errorMsg))(error) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(errorMsg))(ignoreA) }

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log_(F[A]) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] =
      (for {
        _ <- Log[F].log_(Fx[F].effectOf(debugMsg))(debug)
        _ <- Log[F].log_(Fx[F].effectOf(debugMsg))(debug(debugThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(debugMsg))(a => debug(debugThrowable)(s"DEBUG message: $a"))
        _ <- Log[F].log_(Fx[F].effectOf(debugMsg))(ignoreA)
        _ <- Log[F].log_(Fx[F].effectOf(infoMsg))(info)
        _ <- Log[F].log_(Fx[F].effectOf(infoMsg))(info(infoThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(infoMsg))(a => info(infoThrowable)(s"INFO message: $a"))
        _ <- Log[F].log_(Fx[F].effectOf(infoMsg))(ignoreA)
        _ <- Log[F].log_(Fx[F].effectOf(warnMsg))(warn)
        _ <- Log[F].log_(Fx[F].effectOf(warnMsg))(warn(warnThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(warnMsg))(a => warn(warnThrowable)(s"WARN message: $a"))
        _ <- Log[F].log_(Fx[F].effectOf(warnMsg))(ignoreA)
        _ <- Log[F].log_(Fx[F].effectOf(errorMsg))(error)
        _ <- Log[F].log_(Fx[F].effectOf(errorMsg))(error(errorThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(errorMsg))(a => error(errorThrowable)(s"ERROR message: $a"))
        _ <- Log[F].log_(Fx[F].effectOf(errorMsg))(ignoreA)
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(
        debugMsg,
        s"$debugMsg\n${debugThrowable.toString}",
        s"DEBUG message: $debugMsg\n${debugThrowable.toString}",
      ),
      infoMessages = Vector(
        infoMsg,
        s"$infoMsg\n${infoThrowable.toString}",
        s"INFO message: $infoMsg\n${infoThrowable.toString}",
      ),
      warnMessages = Vector(
        warnMsg,
        s"$warnMsg\n${warnThrowable.toString}",
        s"WARN message: $warnMsg\n${warnThrowable.toString}",
      ),
      errorMessages = Vector(
        errorMsg,
        s"$errorMsg\n${errorThrowable.toString}",
        s"ERROR message: $errorMsg\n${errorThrowable.toString}",
      ),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log_(F[A]) with matching cases") {
    val debugMsg  = RandomGens.genAlphaNumericString(20)
    val infoMsg   = RandomGens.genAlphaNumericString(20)
    val warnMsg   = RandomGens.genAlphaNumericString(20)
    val errorMsg  = RandomGens.genAlphaNumericString(20)
    val testCases = RandomGens.genTestCases

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] = {
      val fa = Fx[F].effectOf(testCases)
      Log[F].log_(fa) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          debug(s"$debugMsg / id=${id.toString}, name=$name, not enabled")
      } *> Log[F].log_(fa) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          info(s"$infoMsg / id=${id.toString}, name=$name, not enabled")
      } *> Log[F].log_(fa) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          warn(s"$warnMsg / id=${id.toString}, name=$name, not enabled")
      } *> Log[F].log_(fa) {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          error(s"$errorMsg / id=${id.toString}, name=$name, not enabled")
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

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.logS(String)") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(String, String, String, String)] =
      for {
        msg1 <- Log[F].logS(debugMsg)(debug)
        msg2 <- Log[F].logS(infoMsg)(info)
        msg3 <- Log[F].logS(warnMsg)(warn)
        msg4 <- Log[F].logS(errorMsg)(error)
      } yield (msg1, msg2, msg3, msg4)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    val expected = (debugMsg, infoMsg, warnMsg, errorMsg)

    runLog[Future].map { logs =>
      Assertions.assertEquals(logs, expected)
      Assertions.assertEquals(logger, expectedLogger)
    }

  }

  test("test Log.logS(String) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(String, String, String, String, String, String, String, String)] =
      for {
        msg1a <- Log[F].logS(debugMsg)(debug)
        msg1b <- Log[F].logS(debugMsg)(debug(debugThrowable))
        msg2a <- Log[F].logS(infoMsg)(info)
        msg2b <- Log[F].logS(infoMsg)(info(infoThrowable))
        msg3a <- Log[F].logS(warnMsg)(warn)
        msg3b <- Log[F].logS(warnMsg)(warn(warnThrowable))
        msg4a <- Log[F].logS(errorMsg)(error)
        msg4b <- Log[F].logS(errorMsg)(error(errorThrowable))
      } yield (msg1a, msg1b, msg2a, msg2b, msg3a, msg3b, msg4a, msg4b)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(
        debugMsg,
        s"$debugMsg\n${debugThrowable.toString}",
      ),
      infoMessages = Vector(
        infoMsg,
        s"$infoMsg\n${infoThrowable.toString}",
      ),
      warnMessages = Vector(
        warnMsg,
        s"$warnMsg\n${warnThrowable.toString}",
      ),
      errorMessages = Vector(
        errorMsg,
        s"$errorMsg\n${errorThrowable.toString}",
      ),
    )

    val expected = (debugMsg, debugMsg, infoMsg, infoMsg, warnMsg, warnMsg, errorMsg, errorMsg)
    runLog[Future].map { logs =>
      Assertions.assertEquals(logs, expected)
      Assertions.assertEquals(logger, expectedLogger)
    }

  }

  test("test Log.logS_(String)") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[Unit] =
      Log[F]
        .logS_(debugMsg)(debug)
        .flatMap { _ => Log[F].logS_(infoMsg)(info) }
        .flatMap { _ => Log[F].logS_(warnMsg)(warn) }
        .flatMap { _ => Log[F].logS_(errorMsg)(error) }

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.logS_(String) with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[Unit] =
      Log[F]
        .logS_(debugMsg)(debug)
        .flatMap { _ => Log[F].logS_(debugMsg)(debug(debugThrowable)) }
        .flatMap { _ => Log[F].logS_(infoMsg)(info) }
        .flatMap { _ => Log[F].logS_(infoMsg)(info(infoThrowable)) }
        .flatMap { _ => Log[F].logS_(warnMsg)(warn) }
        .flatMap { _ => Log[F].logS_(warnMsg)(warn(warnThrowable)) }
        .flatMap { _ => Log[F].logS_(errorMsg)(error) }
        .flatMap { _ => Log[F].logS_(errorMsg)(error(errorThrowable)) }

    val expected = LoggerForTesting(
      debugMessages = Vector(
        debugMsg,
        s"$debugMsg\n${debugThrowable.toString}",
      ),
      infoMessages = Vector(
        infoMsg,
        s"$infoMsg\n${infoThrowable.toString}",
      ),
      warnMessages = Vector(
        warnMsg,
        s"$warnMsg\n${warnThrowable.toString}",
      ),
      errorMessages = Vector(
        errorMsg,
        s"$errorMsg\n${errorThrowable.toString}",
      ),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))

  }

  test("test Log.log(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), info)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]]) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), info)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]]) with Throwable") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]]) with Throwable - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, debug)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, info)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, warn)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, debug)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, info)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, warn)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, error)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]])(message, ignore)") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]]) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), error) }

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]]) - None") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), error) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]]) with Throwable - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]]) with Throwable - None") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- Log[F].log_(Fx[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(ignore, message) - Some case") {
    val logMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(ignore, debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, error) }

    val expected = LoggerForTesting(
      debugMessages = Vector(logMsg),
      infoMessages = Vector(logMsg),
      warnMessages = Vector(logMsg),
      errorMessages = Vector(logMsg),
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(ignore, debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, error) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(message, ignore) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.empty,
    )

    runLog[Future](logMsg.some).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Option[A]])(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }

    val expected = LoggerForTesting(
      debugMessages = Vector.empty,
      infoMessages = Vector.empty,
      warnMessages = Vector.empty,
      errorMessages = Vector.fill(4)(ifEmptyMsg),
    )

    runLog[Future](none).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, b => debug(b.toString))
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, b => info(b.toString))
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, b => warn(b.toString))
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, b => error(b.toString))
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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]]) with Throwable") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[Throwable, Int]): F[Either[Throwable, Unit]] = for {
      _ <- Log[F].log(Fx[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => debug(debugThrowable)(b.toString),
           )
      _ <- Log[F].log(Fx[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => info(infoThrowable)(b.toString),
           )
      _ <- Log[F].log(Fx[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => warn(warnThrowable)(b.toString),
           )
      _ <- Log[F].log(Fx[F].effectOf(eab))(
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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(Fx[F].effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- Log[F].log(Fx[F].effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- Log[F].log(Fx[F].effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- Log[F].log(Fx[F].effectOf(eab))(_ => ignore, b => error(b.toString))
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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]])") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(eab))(error, b => debug(b.toString))
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(error, b => info(b.toString)) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(error, b => warn(b.toString)) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(error, b => error(b.toString)) }

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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]]) with Throwable") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[Throwable, Int]): F[Either[Throwable, Unit]] = for {
      _ <- Log[F].log_(Fx[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => debug(debugThrowable)(b.toString),
           )
      _ <- Log[F].log_(Fx[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => info(infoThrowable)(b.toString),
           )
      _ <- Log[F].log_(Fx[F].effectOf(eab))(
             err => error(err)(s"$leftString\n${err.getMessage}"),
             b => warn(warnThrowable)(b.toString),
           )
      _ <- Log[F].log_(Fx[F].effectOf(eab))(
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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(eab))(_ => ignore, b => debug(b.toString))
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(_ => ignore, b => info(b.toString)) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(_ => ignore, b => warn(b.toString)) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(_ => ignore, b => error(b.toString)) }

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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test Log.log_(F[Either[A, B]])(ignore, message)") {
    val rightInt   = RandomGens.genRandomIntWithMinMax(Int.MinValue, Int.MaxValue)
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(eab))(error, _ => ignore)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(error, _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(error, _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(eab))(error, _ => ignore) }

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

    runLog[Future](eab).map(_ => Assertions.assertEquals(logger, expected))
  }

}
