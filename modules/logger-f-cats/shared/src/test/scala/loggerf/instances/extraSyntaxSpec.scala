package loggerf.instances

import _root_.cats.Monad
import _root_.cats.data.{EitherT, OptionT}
import _root_.cats.syntax.either._
import _root_.cats.syntax.flatMap._
import _root_.cats.syntax.functor._
import _root_.cats.syntax.option._
import effectie.core.FxCtor
import effectie.syntax.all._
import extras.concurrent.testing.ConcurrentSupport
import extras.concurrent.testing.types.{ErrorLogger, WaitFor}
import hedgehog._
import hedgehog.runner._
import loggerf.core.Log
import loggerf.logger._
import loggerf.syntax.all._

import java.util.concurrent.ExecutorService
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
object extraSyntaxSpec extends Properties {
  override def tests: List[Test] = List(
    property("test logS(String)(level) with prefix", testLogSWithPrefix),
    property("test logS_(String)(level) with prefix", testLogS_WithPrefix),
    property("test log(F[A]) with prefix", testLogFAWithPrefix),
    property("test log(F[Option[A]]) with prefix", testLogFOptionAWithPrefix),
    property("test log(F[Option[A]])(ignore, message) with prefix", testLogFOptionAIgnoreEmptyWithPrefix),
    property("test log(F[Option[A]])(message, ignore) with prefix", testLogFOptionAIgnoreSomeWithPrefix),
    property("test log(F[Option[A]])(message, ignoreA) with prefix", testLogFOptionAIgnoreASomeWithPrefix),
    property("test log(F[Either[A, B]]) with prefix", testLogFEitherABWithPrefix),
    property("test log(F[Either[A, B]])(ignore, message) with prefix", testLogFEitherABIgnoreLeftWithPrefix),
    property("test log(F[Either[A, B]])(ignoreA, message) with prefix", testLogFEitherABIgnoreALeftWithPrefix),
    property("test log(F[Either[A, B]])(message, ignore) with prefix", testLogFEitherABIgnoreRightWithPrefix),
    property("test log(F[Either[A, B]])(message, ignoreA) with prefix", testLogFEitherABIgnoreARightWithPrefix),
    property("test log(OptionT[F, A]) with prefix", testLogOptionTFAWithPrefix),
    property("test log(OptionT[F, A])(ignore, message) with prefix", testLogOptionTFAIgnoreEmptyWithPrefix),
    property("test log(OptionT[F, A])(message, ignore) with prefix", testLogOptionTFAIgnoreSomeWithPrefix),
    property("test log(EitherT[F, A, B]) with prefix", testLogEitherTFABWithPrefix),
    property("test log(EitherT[F, A, B])(ignore, message) with prefix", testLogEitherTFABIgnoreLeftWithPrefix),
    property("test log(EitherT[F, A, B])(ignoreA, message) with prefix", testLogEitherTFABIgnoreALeftWithPrefix),
    property("test log(EitherT[F, A, B])(message, ignore) with prefix", testLogEitherTFABIgnoreRightWithPrefix),
    property("test log(EitherT[F, A, B])(message, ignoreA) with prefix", testLogEitherTFABIgnoreARightWithPrefix),
  ) ++ List(
    property("test String.logS with prefix", LogExtensionSpec.testStringLogSWithPrefix),
    property("test String.logS_ with prefix", LogExtensionSpec.testStringLogS_WithPrefix),
    property("test F[A].log with prefix", LogExtensionSpec.testFALogWithPrefix),
    property("test F[Option[A]].log with prefix", LogExtensionSpec.testFOptionALogWithPrefix),
    property(
      "test F[Option[A]].log(ignore, message) with prefix",
      LogExtensionSpec.testFOptionALogIgnoreEmptyWithPrefix,
    ),
    property(
      "test F[Option[A]].log(message, ignore) with prefix",
      LogExtensionSpec.testFOptionALogIgnoreSomeWithPrefix,
    ),
    property("test F[Either[A, B]].log", LogExtensionSpec.testFEitherABLogWithPrefix),
    property(
      "test F[Either[A, B]].log(ignore, message) with prefix",
      LogExtensionSpec.testFEitherABLogIgnoreLeftWithPrefix,
    ),
    property(
      "test F[Either[A, B]].log(ignoreA, message) with prefix",
      LogExtensionSpec.testFEitherABLogIgnoreALeftWithPrefix,
    ),
    property(
      "test F[Either[A, B]].log(message, ignore) with prefix",
      LogExtensionSpec.testFEitherABLogIgnoreRightWithPrefix,
    ),
    property(
      "test F[Either[A, B]].log(message, ignoreA) with prefix",
      LogExtensionSpec.testFEitherABLogIgnoreARightWithPrefix,
    ),
    property("test OptionT[F, A].log with prefix", LogExtensionSpec.testLogOptionTFAWithPrefix),
    property(
      "test OptionT[F, A].log(ignore, message) with prefix",
      LogExtensionSpec.testLogOptionTFAIgnoreEmptyWithPrefix,
    ),
    property(
      "test OptionT[F, A].log(message, ignore) with prefix",
      LogExtensionSpec.testLogOptionTFAIgnoreSomeWithPrefix,
    ),
    property("test EitherT[F, A, B].log with prefix", LogExtensionSpec.testLogEitherTFABWithPrefix),
    property(
      "test EitherT[F, A, B].log(ignore, message) with prefix",
      LogExtensionSpec.testLogEitherTFABIgnoreLeftWithPrefix,
    ),
    property(
      "test EitherT[F, A, B].log(ignoreA, message) with prefix",
      LogExtensionSpec.testLogEitherTFABIgnoreALeftWithPrefix,
    ),
    property(
      "test EitherT[F, A, B].log(message, ignore) with prefix",
      LogExtensionSpec.testLogEitherTFABIgnoreRightWithPrefix,
    ),
    property(
      "test EitherT[F, A, B].log(message, ignoreA) with prefix",
      LogExtensionSpec.testLogEitherTFABIgnoreARightWithPrefix,
    ),
  )

  implicit val errorLogger: ErrorLogger[Throwable] = ErrorLogger.printlnDefaultErrorLogger

  private val waitFor500Millis = WaitFor(500.milliseconds)

  def testLogSWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogS_WithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogFAWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogFOptionAWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
      } yield ().some)

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + logMsg),
          infoMessages = Vector(prefixString + logMsg),
          warnMessages = Vector(prefixString + logMsg),
          errorMessages = Vector(prefixString + logMsg),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreEmptyWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(ignore, debug(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, info(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, warn(prefix(prefixString)))
        _ <- log(effectOf(oa))(ignore, error(prefix(prefixString)))
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + logMsg),
          infoMessages = Vector(prefixString + logMsg),
          warnMessages = Vector(prefixString + logMsg),
          errorMessages = Vector(prefixString + logMsg),
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

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreSomeWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
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
          errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreASomeWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(prefix(prefixString))(ifEmptyMsg), ignoreA)
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
          errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFEitherABWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreLeftWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreALeftWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreRightWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreARightWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogOptionTFAWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + logMsg),
          infoMessages = Vector(prefixString + logMsg),
          warnMessages = Vector(prefixString + logMsg),
          errorMessages = Vector(prefixString + logMsg),
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(prefixString + ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogOptionTFAIgnoreEmptyWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, info(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, warn(prefix(prefixString)))
      _ <- log(OptionT(effectOf(oa)))(ignore, error(prefix(prefixString)))
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(prefixString + logMsg),
          infoMessages = Vector(prefixString + logMsg),
          warnMessages = Vector(prefixString + logMsg),
          errorMessages = Vector(prefixString + logMsg),
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

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogOptionTFAIgnoreSomeWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
    } yield ()).value

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
          errorMessages = Vector(prefixString + ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogEitherTFABWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreLeftWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreALeftWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreRightWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreARightWithPrefix: Property = for {
    prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
    rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight      <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
      import effectie.instances.future.fxCtor._
      import loggerf.instances.future.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  // ////

  object LogExtensionSpec {

    def testStringLogSWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testStringLogS_WithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFALogWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFOptionALogWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
        } yield ().some)

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(prefixString + logMsg),
            infoMessages = Vector(prefixString + logMsg),
            warnMessages = Vector(prefixString + logMsg),
            errorMessages = Vector(prefixString + logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogIgnoreEmptyWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- effectOf(oa).log(ignore, debug(prefix(prefixString)))
          _ <- effectOf(oa).log(ignore, info(prefix(prefixString)))
          _ <- effectOf(oa).log(ignore, warn(prefix(prefixString)))
          _ <- effectOf(oa).log(ignore, error(prefix(prefixString)))
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(prefixString + logMsg),
            infoMessages = Vector(prefixString + logMsg),
            warnMessages = Vector(prefixString + logMsg),
            errorMessages = Vector(prefixString + logMsg),
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

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogIgnoreSomeWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
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
            errorMessages = Vector.fill(4)(prefixString + ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFEitherABLogWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreLeftWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreALeftWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreRightWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreARightWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogOptionTFAWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), debug(prefix(prefixString)))
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), info(prefix(prefixString)))
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), warn(prefix(prefixString)))
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), error(prefix(prefixString)))
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(prefixString + logMsg),
            infoMessages = Vector(prefixString + logMsg),
            warnMessages = Vector(prefixString + logMsg),
            errorMessages = Vector(prefixString + logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(prefixString + ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogOptionTFAIgnoreEmptyWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(ignore, debug(prefix(prefixString)))
        _ <- OptionT(effectOf(oa)).log(ignore, info(prefix(prefixString)))
        _ <- OptionT(effectOf(oa)).log(ignore, warn(prefix(prefixString)))
        _ <- OptionT(effectOf(oa)).log(ignore, error(prefix(prefixString)))
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(prefixString + logMsg),
            infoMessages = Vector(prefixString + logMsg),
            warnMessages = Vector(prefixString + logMsg),
            errorMessages = Vector(prefixString + logMsg),
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

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogOptionTFAIgnoreSomeWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      logMsg       <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg   <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(prefix(prefixString))(ifEmptyMsg), _ => ignore)
      } yield ()).value

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
            errorMessages = Vector(prefixString + ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogEitherTFABWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreLeftWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreALeftWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreRightWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreARightWithPrefix: Property = for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      rightInt     <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString   <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight      <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.future.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

  }
}
