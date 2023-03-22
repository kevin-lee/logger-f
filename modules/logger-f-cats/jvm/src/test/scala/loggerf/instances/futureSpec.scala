package loggerf.instances

import _root_.cats.data.{EitherT, OptionT}
import _root_.cats.syntax.either._
import _root_.cats.syntax.option._
import extras.concurrent.testing.ConcurrentSupport
import extras.concurrent.testing.types.{ErrorLogger, WaitFor}
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.instances.future.logFuture
import loggerf.logger._
import loggerf.syntax.all._

import java.util.concurrent.ExecutorService
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
object futureSpec extends Properties {
  override def tests: List[Test] = List(
    property("test Log.log(F[A])", testLogFA),
    property("test Log.log(F[Option[A]])", testLogFOptionA),
    property("test Log.log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test Log.log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test log(OptionT[Future, A]])", testLogOptionTFA),
    property("test log(OptionT[Future, A]])(ignore, message)", testLogOptionTFAIgnoreEmpty),
    property("test log(OptionT[Future, A]])(message, ignore)", testLogOptionTFAIgnoreSome),
    property("test Log.log(F[Either[A, B]])", testLogFEitherAB),
    property("test Log.log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test Log.log(F[Either[A, B]])(message, ignore)", testLogFEitherABIgnoreRight),
    property("test log(EitherT[Future, A, B])", testLogEitherTFAB),
    property("test log(EitherT[Future, A, B]])(ignore, message)", testLogEitherTFABIgnoreLeft),
    property("test log(EitherT[Future, A, B]])(message, ignore)", testLogEitherTFABIgnoreRight),
    property("test Log.log_(F[A])", testLog_FA),
    property("test Log.log_(F[Option[A]])", testLog_FOptionA),
    property("test Log.log_(F[Option[A]])(ignore, message)", testLog_FOptionAIgnoreEmpty),
    property("test Log.log_(F[Option[A]])(message, ignore)", testLog_FOptionAIgnoreSome),
    property("test log_(OptionT[Future, A]])", testLog_OptionTFA),
    property("test log_(OptionT[Future, A]])(ignore, message)", testLog_OptionTFAIgnoreEmpty),
    property("test log_(OptionT[Future, A]])(message, ignore)", testLog_OptionTFAIgnoreSome),
    property("test Log.log_(F[Either[A, B]])", testLog_FEitherAB),
    property("test Log.log_(F[Either[A, B]])(ignore, message)", testLog_FEitherABIgnoreLeft),
    property("test Log.log_(F[Either[A, B]])(message, ignore)", testLog_FEitherABIgnoreRight),
    property("test log_(EitherT[Future, A, B])", testLog_EitherTFAB),
    property("test log_(EitherT[Future, A, B]])(ignore, message)", testLog_EitherTFABIgnoreLeft),
    property("test log_(EitherT[Future, A, B]])(message, ignore)", testLog_EitherTFABIgnoreRight),
  )

  implicit val errorLogger: ErrorLogger[Throwable] = ErrorLogger.printlnDefaultErrorLogger

  private val waitFor500Millis = WaitFor(500.milliseconds)

  def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog: Future[Unit] =
      (for {
        _ <- Log[Future].log(Future(debugMsg))(debug)
        _ <- Log[Future].log(Future(infoMsg))(info)
        _ <- Log[Future].log(Future(warnMsg))(warn)
        _ <- Log[Future].log(Future(errorMsg))(error)
      } yield ())

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog

    }
    logger ==== expected

  }

  def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), debug)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), info)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), warn)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(ignore, debug)
        _ <- Log[Future].log(Future(oa))(ignore, info)
        _ <- Log[Future].log(Future(oa))(ignore, warn)
        _ <- Log[Future].log(Future(oa))(ignore, error)
      } yield ().some)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): Future[Option[Unit]] =
      (for {
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[Future].log(Future(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg)
    }

    logger ==== expected
  }

  def testLogOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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
          errorMessages = Vector.fill(1)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      (for {
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), debug)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), info)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), warn)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), error)
      } yield ())

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg).value
    }

    logger ==== expected
  }

  def testLogOptionTFAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      for {
        _ <- log(OptionT(Future(oa)))(ignore, debug)
        _ <- log(OptionT(Future(oa)))(ignore, info)
        _ <- log(OptionT(Future(oa)))(ignore, warn)
        _ <- log(OptionT(Future(oa)))(ignore, error)
      } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg).value
    }

    logger ==== expected
  }

  def testLogOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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
          errorMessages = Vector.fill(1)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      for {
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
      } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg).value
    }

    logger ==== expected
  }

  def testLogFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): Future[Either[String, Unit]] = for {
      _ <- Log[Future].log(Future(eab))(error, b => debug(b.toString))
      _ <- Log[Future].log(Future(eab))(error, b => info(b.toString))
      _ <- Log[Future].log(Future(eab))(error, b => warn(b.toString))
      _ <- Log[Future].log(Future(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): Future[Either[String, Unit]] = for {
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => debug(b.toString))
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => info(b.toString))
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => warn(b.toString))
      _ <- Log[Future].log(Future(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab)
    }

    logger ==== expected
  }

  def testLogFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): Future[Either[String, Unit]] = for {
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
      _ <- Log[Future].log(Future(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab)
    }

    logger ==== expected
  }

  def testLogEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log(EitherT(Future(eab)))(error, b => debug(b.toString))
      _ <- log(EitherT(Future(eab)))(error, b => info(b.toString))
      _ <- log(EitherT(Future(eab)))(error, b => warn(b.toString))
      _ <- log(EitherT(Future(eab)))(error, b => error(b.toString))
    } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab).value
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => info(b.toString))
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log(EitherT(Future(eab)))(_ => ignore, b => error(b.toString))
    } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab).value
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log(EitherT(Future(eab)))(error, _ => ignore)
    } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab).value
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

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog: Future[Unit] =
      Log[Future]
        .log_(Future(debugMsg))(debug)
        .flatMap { _ => Log[Future].log_(Future(infoMsg))(info) }
        .flatMap { _ => Log[Future].log_(Future(warnMsg))(warn) }
        .flatMap { _ => Log[Future].log_(Future(errorMsg))(error) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog

    }
    logger ==== expected

  }

  def testLog_FOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), error) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg)
    }

    logger ==== expected
  }

  def testLog_FOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(ignore, debug)
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, info) }
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, warn) }
        .flatMap { _ => Log[Future].log_(Future(oa))(ignore, error) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg)
    }

    logger ==== expected
  }

  def testLog_FOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): Future[Unit] =
      Log[Future]
        .log_(Future(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(oa))(error(ifEmptyMsg), _ => ignore) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg)
    }

    logger ==== expected
  }

  def testLog_OptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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
          errorMessages = Vector.fill(1)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(error(ifEmptyMsg), debug)
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), info) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), warn) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), error) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg).value
    }

    logger ==== expected
  }

  def testLog_OptionTFAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(ignore, debug)
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, info) }
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, warn) }
        .flatMap { _ => log_(OptionT(Future(oa)))(ignore, error) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg).value
    }

    logger ==== expected
  }

  def testLog_OptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

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
          errorMessages = Vector.fill(1)(ifEmptyMsg),
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(oa: Option[String]): OptionT[Future, Unit] =
      log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => log_(OptionT(Future(oa)))(error(ifEmptyMsg), _ => ignore) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(logMsg).value
    }

    logger ==== expected
  }

  def testLog_FEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): Future[Unit] =
      Log[Future]
        .log_(Future(eab))(error, b => debug(b.toString))
        .flatMap { _ => Log[Future].log_(Future(eab))(error, b => info(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, b => warn(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, b => error(b.toString)) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab)
    }

    logger ==== expected
  }

  def testLog_FEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): Future[Unit] =
      Log[Future]
        .log_(Future(eab))(_ => ignore, b => debug(b.toString))
        .flatMap { _ => Log[Future].log_(Future(eab))(_ => ignore, b => info(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(_ => ignore, b => warn(b.toString)) }
        .flatMap { _ => Log[Future].log_(Future(eab))(_ => ignore, b => error(b.toString)) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab)
    }

    logger ==== expected
  }

  def testLog_FEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): Future[Unit] =
      Log[Future]
        .log_(Future(eab))(error, _ => ignore)
        .flatMap { _ => Log[Future].log_(Future(eab))(error, _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, _ => ignore) }
        .flatMap { _ => Log[Future].log_(Future(eab))(error, _ => ignore) }

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab)
    }

    logger ==== expected
  }

  ///

  def testLog_EitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log_(EitherT(Future(eab)))(error, b => debug(b.toString))
      _ <- log_(EitherT(Future(eab)))(error, b => info(b.toString))
      _ <- log_(EitherT(Future(eab)))(error, b => warn(b.toString))
      _ <- log_(EitherT(Future(eab)))(error, b => error(b.toString))
    } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab).value
    }

    logger ==== expected
  }

  def testLog_EitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => info(b.toString))
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log_(EitherT(Future(eab)))(_ => ignore, b => error(b.toString))
    } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab).value
    }

    logger ==== expected
  }

  def testLog_EitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    def runLog(eab: Either[String, Int]): EitherT[Future, String, Unit] = for {
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
      _ <- log_(EitherT(Future(eab)))(error, _ => ignore)
    } yield ()

    val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor500Millis) {

      runLog(eab).value
    }

    logger ==== expected
  }

}
