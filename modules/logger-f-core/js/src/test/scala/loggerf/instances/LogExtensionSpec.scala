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
class LogExtensionSpec extends munit.FunSuite {
  implicit val ec: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

  test("test F[A].log") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- FxCtor[F].effectOf(debugMsg).log(debug)
        _ <- FxCtor[F].effectOf(debugMsg).log(ignoreA)
        _ <- FxCtor[F].effectOf(infoMsg).log(info)
        _ <- FxCtor[F].effectOf(infoMsg).log(ignoreA)
        _ <- FxCtor[F].effectOf(warnMsg).log(warn)
        _ <- FxCtor[F].effectOf(warnMsg).log(ignoreA)
        _ <- FxCtor[F].effectOf(errorMsg).log(error)
        _ <- FxCtor[F].effectOf(errorMsg).log(ignoreA)
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

  test("test F[A].log with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def testMessage(s: String): String = s"test message: $s"

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- FxCtor[F].effectOf(debugMsg).log(debug)
        _ <- FxCtor[F].effectOf(debugMsg).log(debugA)
        _ <- FxCtor[F].effectOf(debugMsg).log(a => debug(testMessage(a)))
        _ <- FxCtor[F].effectOf(debugMsg).log(debug(debugThrowable))
        _ <- FxCtor[F].effectOf(debugMsg).log(a => debug(debugThrowable)(testMessage(a)))
        _ <- FxCtor[F].effectOf(debugMsg).log(debugA(debugThrowable))
        _ <- FxCtor[F].effectOf(debugMsg).log(ignoreA)
        //
        _ <- FxCtor[F].effectOf(infoMsg).log(info)
        _ <- FxCtor[F].effectOf(infoMsg).log(infoA)
        _ <- FxCtor[F].effectOf(infoMsg).log(a => info(testMessage(a)))
        _ <- FxCtor[F].effectOf(infoMsg).log(info(infoThrowable))
        _ <- FxCtor[F].effectOf(infoMsg).log(a => info(infoThrowable)(testMessage(a)))
        _ <- FxCtor[F].effectOf(infoMsg).log(infoA(infoThrowable))
        _ <- FxCtor[F].effectOf(infoMsg).log(ignoreA)
        //
        _ <- FxCtor[F].effectOf(warnMsg).log(warn)
        _ <- FxCtor[F].effectOf(warnMsg).log(warnA)
        _ <- FxCtor[F].effectOf(warnMsg).log(a => warn(testMessage(a)))
        _ <- FxCtor[F].effectOf(warnMsg).log(warn(warnThrowable))
        _ <- FxCtor[F].effectOf(warnMsg).log(a => warn(warnThrowable)(testMessage(a)))
        _ <- FxCtor[F].effectOf(warnMsg).log(warnA(warnThrowable))
        _ <- FxCtor[F].effectOf(warnMsg).log(ignoreA)
        //
        _ <- FxCtor[F].effectOf(errorMsg).log(error)
        _ <- FxCtor[F].effectOf(errorMsg).log(errorA)
        _ <- FxCtor[F].effectOf(errorMsg).log(a => error(testMessage(a)))
        _ <- FxCtor[F].effectOf(errorMsg).log(error(errorThrowable))
        _ <- FxCtor[F].effectOf(errorMsg).log(a => error(errorThrowable)(testMessage(a)))
        _ <- FxCtor[F].effectOf(errorMsg).log(errorA(errorThrowable))
        _ <- FxCtor[F].effectOf(errorMsg).log(ignoreA)
      } yield ())

    val expected = LoggerForTesting(
      debugMessages = Vector(
        debugMsg,
        debugMsg,
        testMessage(debugMsg),
        debugMsg + "\n" + debugThrowable.toString,
        testMessage(debugMsg) + "\n" + debugThrowable.toString,
        debugMsg + "\n" + debugThrowable.toString,
      ),
      infoMessages = Vector(
        infoMsg,
        infoMsg,
        testMessage(infoMsg),
        infoMsg + "\n" + infoThrowable.toString,
        testMessage(infoMsg) + "\n" + infoThrowable.toString,
        infoMsg + "\n" + infoThrowable.toString,
      ),
      warnMessages = Vector(
        warnMsg,
        warnMsg,
        testMessage(warnMsg),
        warnMsg + "\n" + warnThrowable.toString,
        testMessage(warnMsg) + "\n" + warnThrowable.toString,
        warnMsg + "\n" + warnThrowable.toString,
      ),
      errorMessages = Vector(
        errorMsg,
        errorMsg,
        testMessage(errorMsg),
        errorMsg + "\n" + errorThrowable.toString,
        testMessage(errorMsg) + "\n" + errorThrowable.toString,
        errorMsg + "\n" + errorThrowable.toString,
      ),
    )

    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test F[A].log with matching cases") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val testCases = RandomGens.genTestCases

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      (for {
        _ <- FxCtor[F].effectOf(testCases).log {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 debug(s"$debugMsg / id=${id.toString}, name=$name, not enabled")
             }
        _ <- FxCtor[F].effectOf(testCases).log {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 info(s"$infoMsg / id=${id.toString}, name=$name, not enabled")
             }
        _ <- FxCtor[F].effectOf(testCases).log {
               case TestCases(_, _, true) =>
                 ignore
               case TestCases(id, name, false) =>
                 warn(s"$warnMsg / id=${id.toString}, name=$name, not enabled")
             }
        _ <- FxCtor[F].effectOf(testCases).log {
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

  test("test F[A].log_") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      FxCtor[F]
        .effectOf(debugMsg)
        .log_(debug)
        .flatMap { _ => FxCtor[F].effectOf(debugMsg).log_(ignoreA) }
        .flatMap { _ => FxCtor[F].effectOf(infoMsg).log_(info) }
        .flatMap { _ => FxCtor[F].effectOf(infoMsg).log_(ignoreA) }
        .flatMap { _ => FxCtor[F].effectOf(warnMsg).log_(warn) }
        .flatMap { _ => FxCtor[F].effectOf(warnMsg).log_(ignoreA) }
        .flatMap { _ => FxCtor[F].effectOf(errorMsg).log_(error) }
        .flatMap { _ => FxCtor[F].effectOf(errorMsg).log_(ignoreA) }

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test F[A].log_ with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      FxCtor[F]
        .effectOf(debugMsg)
        .log_(debug(debugThrowable))
        .flatMap { _ => FxCtor[F].effectOf(infoMsg).log_(info(infoThrowable)) }
        .flatMap { _ => FxCtor[F].effectOf(warnMsg).log_(warn(warnThrowable)) }
        .flatMap { _ => FxCtor[F].effectOf(errorMsg).log_(error(errorThrowable)) }

    val expected = LoggerForTesting(
      debugMessages = Vector(s"$debugMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(s"$infoMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(s"$warnMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(s"$errorMsg\n${errorThrowable.toString}"),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expected))
  }

  test("test F[A].log_ with matching cases") {
    val debugMsg  = RandomGens.genAlphaNumericString(20)
    val infoMsg   = RandomGens.genAlphaNumericString(20)
    val warnMsg   = RandomGens.genAlphaNumericString(20)
    val errorMsg  = RandomGens.genAlphaNumericString(20)
    val testCases = RandomGens.genTestCases

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] = {
      val fa = FxCtor[F].effectOf(testCases)

      fa.log_ {
        case TestCases(_, _, true) =>
          ignore
        case TestCases(id, name, false) =>
          debug(s"$debugMsg / id=${id.toString}, name=$name, not enabled")
      }.flatMap { _ =>
        fa.log_ {
          case TestCases(_, _, true) =>
            ignore
          case TestCases(id, name, false) =>
            info(s"$infoMsg / id=${id.toString}, name=$name, not enabled")
        }
      }.flatMap { _ =>
        fa.log_ {
          case TestCases(_, _, true) =>
            ignore
          case TestCases(id, name, false) =>
            warn(s"$warnMsg / id=${id.toString}, name=$name, not enabled")
        }
      }.flatMap { _ =>
        fa.log_ {
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

  test("test String.logS") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(String, String, String, String)] =
      for {
        msg1 <- debugMsg.logS(debug)
        msg2 <- infoMsg.logS(info)
        msg3 <- warnMsg.logS(warn)
        msg4 <- errorMsg.logS(error)
      } yield (msg1, msg2, msg3, msg4)

    val expectedLogger = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import loggerf.instances.future.logFuture
    runLog[Future].map(_ => Assertions.assertEquals(logger, expectedLogger))
  }

  test("test String.logS with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(String, String, String, String)] =
      for {
        msg1 <- debugMsg.logS(debug(debugThrowable))
        msg2 <- infoMsg.logS(info(infoThrowable))
        msg3 <- warnMsg.logS(warn(warnThrowable))
        msg4 <- errorMsg.logS(error(errorThrowable))
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

  test("test String.logS_") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(Unit, Unit, Unit, Unit)] =
      for {
        r1 <- debugMsg.logS_(debug)
        r2 <- infoMsg.logS_(info)
        r3 <- warnMsg.logS_(warn)
        r4 <- errorMsg.logS_(error)
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

  test("test String.logS_ with Throwable") {
    val debugMsg = RandomGens.genAlphaNumericString(20)
    val infoMsg  = RandomGens.genAlphaNumericString(20)
    val warnMsg  = RandomGens.genAlphaNumericString(20)
    val errorMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: Monad]: F[(Unit, Unit, Unit, Unit)] =
      for {
        r1 <- debugMsg.logS_(debug(debugThrowable))
        r2 <- infoMsg.logS_(info(infoThrowable))
        r3 <- warnMsg.logS_(warn(warnThrowable))
        r4 <- errorMsg.logS_(error(errorThrowable))
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

  test("test F[Option[A]].log Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), debug)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), info)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), warn)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), error)
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

  test("test F[Option[A]].log - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), debug)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), info)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), warn)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), error)
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

  test("test F[Option[A]].log with Throwable - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
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

  test("test F[Option[A]].log with Throwable - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
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

  test("test F[Option[A]].log(ignore, message) - Some case") {
    val logMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- FxCtor[F].effectOf(oa).log(ignore, debug)
        _ <- FxCtor[F].effectOf(oa).log(ignore, info)
        _ <- FxCtor[F].effectOf(oa).log(ignore, warn)
        _ <- FxCtor[F].effectOf(oa).log(ignore, error)
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

  test("test F[Option[A]].log(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- FxCtor[F].effectOf(oa).log(ignore, debug)
        _ <- FxCtor[F].effectOf(oa).log(ignore, info)
        _ <- FxCtor[F].effectOf(oa).log(ignore, warn)
        _ <- FxCtor[F].effectOf(oa).log(ignore, error)
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

  test("test F[Option[A]].log(message, ignore) - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
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

  test("test F[Option[A]].log(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
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

  test("test F[Option[A]].log_ - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(error(ifEmptyMsg), debug)
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), info) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), warn) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), error) }

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

  test("test F[Option[A]].log_ - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(error(ifEmptyMsg), debug)
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), info) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), warn) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), error) }

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

  test("test F[Option[A]].log_ with Throwable - Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), info(infoThrowable)) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable)) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), error(errorThrowable)) }

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

  test("test F[Option[A]].log_ with Throwable - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), info(infoThrowable)) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable)) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), error(errorThrowable)) }

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

  test("test F[Option[A]].log_(ignore, message) - Some case") {
    val logMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(ignore, debug)
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, info) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, warn) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, error) }

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

  test("test F[Option[A]].log_(ignore, message) - None case") {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(ignore, debug)
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, info) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, warn) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, error) }

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

  test("test F[Option[A]].log_(message, ignore) Some case") {
    val logMsg     = RandomGens.genAlphaNumericString(20)
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }

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

  test("test F[Option[A]].log_(message, ignore) - None case") {
    val ifEmptyMsg = RandomGens.genAlphaNumericString(20)

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      FxCtor[F]
        .effectOf(oa)
        .log_(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }

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

  test("test F[Either[A, B]].log") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- FxCtor[F].effectOf(eab).log(error, b => debug(b.toString))
      _ <- FxCtor[F].effectOf(eab).log(error, b => info(b.toString))
      _ <- FxCtor[F].effectOf(eab).log(error, b => warn(b.toString))
      _ <- FxCtor[F].effectOf(eab).log(error, b => error(b.toString))
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

  test("test F[Either[A, B]].log with Throwable") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[Throwable, Int]): F[Either[Throwable, Unit]] =
      for {
        _ <- FxCtor[F]
               .effectOf(eab)
               .log(err => error(err)(s"$leftString\n${err.getMessage}"), b => debug(debugThrowable)(b.toString))
        _ <- FxCtor[F]
               .effectOf(eab)
               .log(err => error(err)(s"$leftString\n${err.getMessage}"), b => info(infoThrowable)(b.toString))
        _ <- FxCtor[F]
               .effectOf(eab)
               .log(err => error(err)(s"$leftString\n${err.getMessage}"), b => warn(warnThrowable)(b.toString))
        _ <- FxCtor[F]
               .effectOf(eab)
               .log(err => error(err)(s"$leftString\n${err.getMessage}"), b => error(errorThrowable)(b.toString))
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

  test("test F[Either[A, B]].log(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- FxCtor[F].effectOf(eab).log(_ => ignore, b => debug(b.toString))
      _ <- FxCtor[F].effectOf(eab).log(_ => ignore, b => info(b.toString))
      _ <- FxCtor[F].effectOf(eab).log(_ => ignore, b => warn(b.toString))
      _ <- FxCtor[F].effectOf(eab).log(_ => ignore, b => error(b.toString))
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

  test("test F[Either[A, B]].log(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- FxCtor[F].effectOf(eab).log(error, _ => ignore)
      _ <- FxCtor[F].effectOf(eab).log(error, _ => ignore)
      _ <- FxCtor[F].effectOf(eab).log(error, _ => ignore)
      _ <- FxCtor[F].effectOf(eab).log(error, _ => ignore)
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

  test("test F[Either[A, B]].log_") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Unit] =
      FxCtor[F]
        .effectOf(eab)
        .log_(error, b => debug(b.toString))
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(error, b => info(b.toString)) }
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(error, b => warn(b.toString)) }
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(error, b => error(b.toString)) }

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

  test("test F[Either[A, B]].log_ with Throwable") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    val throwables                                                     = RandomGens.genThrowable
    val (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[Throwable, Int]): F[Unit] =
      FxCtor[F]
        .effectOf(eab)
        .log_(err => error(err)(s"$leftString\n${err.getMessage}"), b => debug(debugThrowable)(b.toString))
        .flatMap { _ =>
          FxCtor[F]
            .effectOf(eab)
            .log_(err => error(err)(s"$leftString\n${err.getMessage}"), b => info(infoThrowable)(b.toString))
        }
        .flatMap { _ =>
          FxCtor[F]
            .effectOf(eab)
            .log_(err => error(err)(s"$leftString\n${err.getMessage}"), b => warn(warnThrowable)(b.toString))
        }
        .flatMap { _ =>
          FxCtor[F]
            .effectOf(eab)
            .log_(err => error(err)(s"$leftString\n${err.getMessage}"), b => error(errorThrowable)(b.toString))
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

  test("test F[Either[A, B]].log_(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Unit] =
      FxCtor[F]
        .effectOf(eab)
        .log_(_ => ignore, b => debug(b.toString))
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(_ => ignore, b => info(b.toString)) }
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(_ => ignore, b => warn(b.toString)) }
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(_ => ignore, b => error(b.toString)) }

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

  test("test F[Either[A, B]].log_(ignore, message)") {
    val rightInt   = RandomGens.genRandomInt()
    val leftString = RandomGens.genAlphaNumericString(20)
    val isRight    = RandomGens.genBoolean()

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Unit] =
      FxCtor[F]
        .effectOf(eab)
        .log_(error, _ => ignore)
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(error, _ => ignore) }
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(error, _ => ignore) }
        .flatMap { _ => FxCtor[F].effectOf(eab).log_(error, _ => ignore) }

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
