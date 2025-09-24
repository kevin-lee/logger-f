package loggerf.instances

import cats.Monad
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.instances.future.fxCtor._
import extras.concurrent.testing.ConcurrentSupport
import extras.concurrent.testing.types.{ErrorLogger, WaitFor}
import hedgehog._
import hedgehog.runner._
import loggerf.core.Log
import loggerf.core.syntax.all._
import loggerf.logger._
import loggerf.test_data
import loggerf.test_data.{Gens, TestCases}

import java.util.concurrent.ExecutorService
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
object syntaxSpec extends Properties {
  override def tests: List[Test] = List(
    property("test log(F[A])", testLogFA),
    property("test log(F[A]) with Throwable", testLogFAWithThrowable),
    property("test log(F[A]) with matching cases", testLogFAWithMatchingCases),
    property("test log_(F[A])", testLog_FA),
    property("test log_(F[A]) with Throwable", testLog_FAWithThrowable),
    property("test log_(F[A]) with matching cases", testLog_FAWithMatchingCases),
    property("test log(String)", testLogString),
    property("test log(String) with Throwable", testLogStringWithThrowable),
    property("test log_(String)", testLog_String),
    property("test log_(String) with Throwable", testLog_StringWithThrowable),
    property("test log(F[Option[A]])", testLogFOptionA),
    property("test log(F[Option[A]]) with Throwable", testLogFOptionAWithThrowable),
    property("test log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test log_(F[Option[A]])", testLog_FOptionA),
    property("test log_(F[Option[A]] with Throwable)", testLog_FOptionAWithThrowable),
    property("test log_(F[Option[A]])(ignore, message)", testLog_FOptionAIgnoreEmpty),
    property("test log_(F[Option[A]])(message, ignore)", testLog_FOptionAIgnoreSome),
    property("test log(F[Either[A, B]])", testLogFEitherAB),
    property("test log(F[Either[A, B]]) with Throwable", testLogFEitherABWithThrowable),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
    property("test log_(F[Either[A, B]])", testLog_FEitherAB),
    property("test log_(F[Either[A, B]]) with Throwable", testLog_FEitherABWithThrowable),
    property("test log_(F[Either[A, B]])(ignore, message)", testLog_FEitherABIgnoreLeft),
    property("test log_(F[Either[A, B]])(ignore, message)", testLog_FEitherABIgnoreRight),
  ) ++ List(
    property("test F[A].log", LogExtensionSpec.testFALog),
    property("test F[A].log with Throwable", LogExtensionSpec.testFALogWithThrowable),
    property("test F[A].log with matching cases", LogExtensionSpec.testFALogWithMatchingCases),
    property("test F[A].log_", LogExtensionSpec.testFALog_()),
    property("test F[A].log_ with Throwable", LogExtensionSpec.testFALog_WithThrowable()),
    property("test F[A].log_ with matching cases", LogExtensionSpec.testFALog_WithMatchingCases()),
    property("test String.logS", LogExtensionSpec.testStringLog),
    property("test String.logS with Throwable", LogExtensionSpec.testStringLogWithThrowable),
    property("test String.logS_", LogExtensionSpec.testStringLog_()),
    property("test String.logS_ with Throwable", LogExtensionSpec.testStringLog_WithThrowable()),
    property("test F[Option[A]].log", LogExtensionSpec.testFOptionALog),
    property("test F[Option[A]].log with Throwable", LogExtensionSpec.testFOptionALogWithThrowable),
    property("test F[Option[A]].log(ignore, message)", LogExtensionSpec.testFOptionALogIgnoreEmpty),
    property("test F[Option[A]].log(message, ignore)", LogExtensionSpec.testFOptionALogIgnoreSome),
    property("test F[Option[A]].log_", LogExtensionSpec.testFOptionALog_()),
    property("test F[Option[A]].log_ with Throwable", LogExtensionSpec.testFOptionALog_WithThrowable()),
    property("test F[Option[A]].log_(ignore, message)", LogExtensionSpec.testFOptionALog_IgnoreEmpty),
    property("test F[Option[A]].log_(message, ignore)", LogExtensionSpec.testFOptionALog_IgnoreSome),
    property("test F[Either[A, B]].log", LogExtensionSpec.testFEitherABLog),
    property("test F[Either[A, B]].log with Throwable", LogExtensionSpec.testFEitherABLogWithThrowable),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testFEitherABLogIgnoreLeft),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testFEitherABLogIgnoreRight),
    property("test F[Either[A, B]].log_", LogExtensionSpec.testFEitherABLog_()),
    property("test F[Either[A, B]].log_ with Throwable", LogExtensionSpec.testFEitherABLog_WithThrowable()),
    property("test F[Either[A, B]].log_(ignore, message)", LogExtensionSpec.testFEitherABLog_IgnoreLeft),
    property("test F[Either[A, B]].log_(ignore, message)", LogExtensionSpec.testFEitherABLog_IgnoreRight),
  )

  implicit val errorLogger: ErrorLogger[Throwable] = ErrorLogger.printlnDefaultErrorLogger

  private val waitFor300Millis = WaitFor(400.milliseconds)

  def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogFAWithThrowable: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogFAWithMatchingCases: Property = for {
    debugMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    testCases <- test_data.Gens.genTestCases.log("testCases")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLog_FA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLog_FAWithThrowable: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLog_FAWithMatchingCases: Property = for {
    debugMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    testCases <- test_data.Gens.genTestCases.log("testCases")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogString: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val expected = (debugMsg, infoMsg, warnMsg, errorMsg)
    val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    actual ==== expected and logger ==== expectedLogger
  }

  def testLogStringWithThrowable: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val expected = (debugMsg, infoMsg, warnMsg, errorMsg)
    val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    (actual ==== expected)
      .log("actual ==== expected") and (logger ==== expectedLogger).log("logger ==== expectedLogger")
  }

  def testLog_String: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val expected = ((), (), (), ())
    val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    actual ==== expected and logger ==== expectedLogger
  }

  def testLog_StringWithThrowable: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val expected = ((), (), (), ())
    val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    actual ==== expected and logger ==== expectedLogger
  }

  def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAWithThrowable: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
        _ <- log(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
      } yield ().some)

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
          infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
          warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
          errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(FxCtor[F].effectOf(oa))(ignore, debug)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, info)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, warn)
        _ <- log(FxCtor[F].effectOf(oa))(ignore, error)
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLog_FOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), error) }

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLog_FOptionAWithThrowable: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), info(infoThrowable)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable)) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(errorThrowable)(ifEmptyMsg), error(errorThrowable)) }

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
          infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
          warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
          errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLog_FOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(ignore, debug)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, info) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, warn) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(ignore, error) }

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLog_FOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
      log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(FxCtor[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }

    val expected = logMsg match {
      case Some(logMsg @ _) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty,
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABWithThrowable: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLog_FEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLog_FEitherABWithThrowable: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")

    throwables <- Gens.genThrowable.log("throwables")
    (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLog_FEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLog_FEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  // ////

  object LogExtensionSpec {

    def testFALog: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFALogWithThrowable: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFALogWithMatchingCases: Property = for {
      debugMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
      testCases <- test_data.Gens.genTestCases.log("testCases")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFALog_(): Property =
      for {
        debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
        infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
        warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
        errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
      } yield {

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

        implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
        implicit val ec: ExecutionContext =
          ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

        ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
          import loggerf.instances.future.logFuture
          runLog[Future]
        }

        logger ==== expected
      }

    def testFALog_WithThrowable(): Property =
      for {
        debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
        infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
        warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
        errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

        throwables <- Gens.genThrowable.log("throwables")
        (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
      } yield {

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

        implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
        implicit val ec: ExecutionContext =
          ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

        ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
          import loggerf.instances.future.logFuture
          runLog[Future]
        }

        logger ==== expected
      }

    def testFALog_WithMatchingCases(): Property =
      for {
        debugMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
        infoMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
        warnMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
        errorMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
        testCases <- test_data.Gens.genTestCases.log("testCases")
      } yield {

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

        implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
        implicit val ec: ExecutionContext =
          ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

        ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
          import loggerf.instances.future.logFuture
          runLog[Future]
        }

        logger ==== expected
      }

    def testStringLog: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val expected = (debugMsg, infoMsg, warnMsg, errorMsg)
      val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      actual ==== expected and logger ==== expectedLogger
    }

    def testStringLogWithThrowable: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val expected = (debugMsg, infoMsg, warnMsg, errorMsg)
      val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      (actual ==== expected)
        .log("actual ==== expected") and (logger ==== expectedLogger).log("logger ==== expectedLogger")
    }

    def testStringLog_(): Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val expected = ((), (), (), ())
      val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      actual ==== expected and logger ==== expectedLogger
    }

    def testStringLog_WithThrowable(): Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val expected = ((), (), (), ())
      val actual   = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      (actual ==== expected)
        .log("actual ==== expected") and (logger ==== expectedLogger).log("logger ==== expectedLogger")
    }

    def testFOptionALog: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), debug)
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), info)
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), warn)
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), error)
        } yield ().some)

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogWithThrowable: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
          _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), info(infoThrowable))
          _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable))
          _ <- FxCtor[F].effectOf(oa).log(error(errorThrowable)(ifEmptyMsg), error(errorThrowable))
        } yield ().some)

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
            infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
            warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
            errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- FxCtor[F].effectOf(oa).log(ignore, debug)
          _ <- FxCtor[F].effectOf(oa).log(ignore, info)
          _ <- FxCtor[F].effectOf(oa).log(ignore, warn)
          _ <- FxCtor[F].effectOf(oa).log(ignore, error)
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty,
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- FxCtor[F].effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg @ _) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty,
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALog_(): Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
        FxCtor[F]
          .effectOf(oa)
          .log_(error(ifEmptyMsg), debug)
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), info) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), warn) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), error) }

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALog_WithThrowable(): Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
        FxCtor[F]
          .effectOf(oa)
          .log_(error(errorThrowable)(ifEmptyMsg), debug(debugThrowable))
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), info(infoThrowable)) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), warn(warnThrowable)) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(errorThrowable)(ifEmptyMsg), error(errorThrowable)) }

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(s"$logMsg\n${debugThrowable.toString}"),
            infoMessages = Vector(s"$logMsg\n${infoThrowable.toString}"),
            warnMessages = Vector(s"$logMsg\n${warnThrowable.toString}"),
            errorMessages = Vector(s"$logMsg\n${errorThrowable.toString}"),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(s"$ifEmptyMsg\n${errorThrowable.toString}"),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALog_IgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
        FxCtor[F]
          .effectOf(oa)
          .log_(ignore, debug)
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, info) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, warn) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(ignore, error) }

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty,
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALog_IgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Unit] =
        FxCtor[F]
          .effectOf(oa)
          .log_(error(ifEmptyMsg), _ => ignore)
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }
          .flatMap { _ => FxCtor[F].effectOf(oa).log_(error(ifEmptyMsg), _ => ignore) }

      val expected = logMsg match {
        case Some(logMsg @ _) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty,
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFEitherABLog: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogWithThrowable: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLog_(): Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLog_WithThrowable(): Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")

      throwables <- Gens.genThrowable.log("throwables")
      (debugThrowable, infoThrowable, warnThrowable, errorThrowable) = throwables
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLog_IgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLog_IgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

  }
}
