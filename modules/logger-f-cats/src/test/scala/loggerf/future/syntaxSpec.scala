package loggerf.future

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import effectie.core.FxCtor
import effectie.syntax.all._
import extras.concurrent.testing.ConcurrentSupport
import extras.concurrent.testing.types.{ErrorLogger, WaitFor}
import hedgehog._
import hedgehog.runner._
import loggerf.cats.syntax.all._
import loggerf.core.Log
import loggerf.logger._

import java.util.concurrent.ExecutorService
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
object syntaxSpec extends Properties {
  override def tests: List[Test] = List(
    property("test log(F[A])", testLogFA),
    property("test logPure(F[A])", testLogPureFA),
    property("test log(F[Option[A]])", testLogFOptionA),
    property("test logPure(F[Option[A]])", testLogPureFOptionA),
    property("test log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test logPure(F[Option[A]])(ignore, message)", testLogPureFOptionAIgnoreEmpty),
    property("test log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test logPure(F[Option[A]])(message, ignore)", testLogPureFOptionAIgnoreSome),
    property("test log(F[Either[A, B]])", testLogFEitherAB),
    property("test logPure(F[Either[A, B]])", testLogPureFEitherAB),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test logPure(F[Either[A, B]])(ignore, message)", testLogPureFEitherABIgnoreLeft),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
    property("test logPure(F[Either[A, B]])(ignore, message)", testLogPureFEitherABIgnoreRight),
    property("test log(OptionT[F, A])", testLogOptionTFA),
    property("test logPure(OptionT[F, A])", testLogPureOptionTFA),
    property("test log(OptionT[F, A])(ignore, message)", testLogOptionTFAIgnoreEmpty),
    property("test logPure(OptionT[F, A])(ignore, message)", testLogPureOptionTFAIgnoreEmpty),
    property("test log(OptionT[F, A])(message, ignore)", testLogOptionTFAIgnoreSome),
    property("test logPure(OptionT[F, A])(message, ignore)", testLogPureOptionTFAIgnoreSome),
    property("test log(EitherT[F, A, B])", testLogEitherTFAB),
    property("test logPure(EitherT[F, A, B])", testLogPureEitherTFAB),
    property("test log(EitherT[F, A, B])(ignore, message)", testLogEitherTFABIgnoreLeft),
    property("test logPure(EitherT[F, A, B])(ignore, message)", testLogPureEitherTFABIgnoreLeft),
    property("test log(EitherT[F, A, B])(message, ignore)", testLogEitherTFABIgnoreRight),
    property("test logPure(EitherT[F, A, B])(message, ignore)", testLogPureEitherTFABIgnoreRight),
  ) ++ List(
    property("test F[A].log", LogExtensionSpec.testFALog),
    property("test F[A].logPure", LogExtensionSpec.testFALogPure),
    property("test F[Option[A]].log", LogExtensionSpec.testFOptionALog),
    property("test F[Option[A]].logPure", LogExtensionSpec.testFOptionALogPure),
    property("test F[Option[A]].log(ignore, message)", LogExtensionSpec.testFOptionALogIgnoreEmpty),
    property("test F[Option[A]].logPure(ignore, message)", LogExtensionSpec.testFOptionALogPureIgnoreEmpty),
    property("test F[Option[A]].log(message, ignore)", LogExtensionSpec.testFOptionALogIgnoreSome),
    property("test F[Option[A]].logPure(message, ignore)", LogExtensionSpec.testFOptionALogPureIgnoreSome),
    property("test F[Either[A, B]].log", LogExtensionSpec.testFEitherABLog),
    property("test F[Either[A, B]].logPure", LogExtensionSpec.testFEitherABLogPure),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testFEitherABLogIgnoreLeft),
    property("test F[Either[A, B]].logPure(ignore, message)", LogExtensionSpec.testFEitherABLogPureIgnoreLeft),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testFEitherABLogIgnoreRight),
    property("test F[Either[A, B]].logPure(ignore, message)", LogExtensionSpec.testFEitherABLogPureIgnoreRight),
    property("test OptionT[F, A].log", LogExtensionSpec.testLogOptionTFA),
    property("test OptionT[F, A].logPure", LogExtensionSpec.testLogPureOptionTFA),
    property("test OptionT[F, A].log(ignore, message)", LogExtensionSpec.testLogOptionTFAIgnoreEmpty),
    property("test OptionT[F, A].logPure(ignore, message)", LogExtensionSpec.testLogPureOptionTFAIgnoreEmpty),
    property("test OptionT[F, A].log(message, ignore)", LogExtensionSpec.testLogOptionTFAIgnoreSome),
    property("test OptionT[F, A].logPure(message, ignore)", LogExtensionSpec.testLogPureOptionTFAIgnoreSome),
    property("test EitherT[F, A, B].log", LogExtensionSpec.testLogEitherTFAB),
    property("test EitherT[F, A, B].logPure", LogExtensionSpec.testLogPureEitherTFAB),
    property("test EitherT[F, A, B].log(ignore, message)", LogExtensionSpec.testLogEitherTFABIgnoreLeft),
    property("test EitherT[F, A, B].logPure(ignore, message)", LogExtensionSpec.testLogPureEitherTFABIgnoreLeft),
    property("test EitherT[F, A, B].log(message, ignore)", LogExtensionSpec.testLogEitherTFABIgnoreRight),
    property("test EitherT[F, A, B].logPure(message, ignore)", LogExtensionSpec.testLogPureEitherTFABIgnoreRight)
  )

  implicit val errorLogger: ErrorLogger[Throwable] = ErrorLogger.printlnDefaultErrorLogger

  private val waitFor300Millis = WaitFor(300.milliseconds)

  def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

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
      errorMessages = Vector(errorMsg)
    )

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogPureFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
      for {
        _ <- logPure(pureOf(debugMsg))(debug)
        _ <- logPure(pureOf(infoMsg))(info)
        _ <- logPure(pureOf(warnMsg))(warn)
        _ <- logPure(pureOf(errorMsg))(error)
      } yield ()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg)
    )

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future]
    }

    logger ==== expected
  }

  def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogPureFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), debug)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), info)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), warn)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), error)
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
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
        _ <- log(effectOf(oa))(ignore, debug)
        _ <- log(effectOf(oa))(ignore, info)
        _ <- log(effectOf(oa))(ignore, warn)
        _ <- log(effectOf(oa))(ignore, error)
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogPureFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      for {
        _ <- logPure(pureOf(oa))(ignore, debug)
        _ <- logPure(pureOf(oa))(ignore, info)
        _ <- logPure(pureOf(oa))(ignore, warn)
        _ <- logPure(pureOf(oa))(ignore, error)
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
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
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogPureFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
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
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogPureFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(error, b => info(b.toString))
      _ <- logPure(pureOf(eab))(error, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
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
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogPureFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => info(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
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
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogPureFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.fill(4)(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogPureOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), debug)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), info)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), warn)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogOptionTFAIgnoreEmpty: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log(OptionT(effectOf(oa)))(ignore, info)
      _ <- log(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log(OptionT(effectOf(oa)))(ignore, error)
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogPureOptionTFAIgnoreEmpty: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(ignore, debug)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, info)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, warn)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, error)
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector(logMsg),
          infoMessages = Vector(logMsg),
          warnMessages = Vector(logMsg),
          errorMessages = Vector(logMsg)
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogPureOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    val expected = logMsg match {
      case Some(logMsg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case None =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(ifEmptyMsg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogPureEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogPureEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector(n.toString),
          infoMessages = Vector(n.toString),
          warnMessages = Vector(n.toString),
          errorMessages = Vector(n.toString)
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
      runLog[Future](eab)
    }

    logger ==== expected
  }

  def testLogPureEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    val expected = eab match {
      case Right(n) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector.empty
        )

      case Left(msg) =>
        LoggerForTesting(
          debugMessages = Vector.empty,
          infoMessages = Vector.empty,
          warnMessages = Vector.empty,
          errorMessages = Vector(msg)
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      import loggerf.future.instances.logFuture
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
          _ <- effectOf(debugMsg).log(debug)
          _ <- effectOf(infoMsg).log(info)
          _ <- effectOf(warnMsg).log(warn)
          _ <- effectOf(errorMsg).log(error)
        } yield ())

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg)
      )

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFALogPure: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
        for {
          _ <- pureOf(debugMsg).logPure(debug)
          _ <- pureOf(infoMsg).logPure(info)
          _ <- pureOf(warnMsg).logPure(warn)
          _ <- pureOf(errorMsg).logPure(error)
        } yield ()

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg)
      )

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future]
      }

      logger ==== expected
    }

    def testFOptionALog: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), debug)
          _ <- effectOf(oa).log(error(ifEmptyMsg), info)
          _ <- effectOf(oa).log(error(ifEmptyMsg), warn)
          _ <- effectOf(oa).log(error(ifEmptyMsg), error)
        } yield ().some)

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogPure: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), debug)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), info)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), warn)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), error)
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
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
          _ <- effectOf(oa).log(ignore, debug)
          _ <- effectOf(oa).log(ignore, info)
          _ <- effectOf(oa).log(ignore, warn)
          _ <- effectOf(oa).log(ignore, error)
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogPureIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- pureOf(oa).logPure(ignore, debug)
          _ <- pureOf(oa).logPure(ignore, info)
          _ <- pureOf(oa).logPure(ignore, warn)
          _ <- pureOf(oa).logPure(ignore, error)
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
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
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        } yield ().some

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALogPureIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
        } yield ().some)

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
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
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogPure: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(error, b => debug(b.toString))
        _ <- pureOf(eab).logPure(error, b => info(b.toString))
        _ <- pureOf(eab).logPure(error, b => warn(b.toString))
        _ <- pureOf(eab).logPure(error, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector(n.toString),
            infoMessages = Vector(n.toString),
            warnMessages = Vector(n.toString),
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
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
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogPureIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(_ => ignore, b => debug(b.toString))
        _ <- pureOf(eab).logPure(_ => ignore, b => info(b.toString))
        _ <- pureOf(eab).logPure(_ => ignore, b => warn(b.toString))
        _ <- pureOf(eab).logPure(_ => ignore, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector(n.toString),
            infoMessages = Vector(n.toString),
            warnMessages = Vector(n.toString),
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
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
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogPureIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(error, _ => ignore)
        _ <- pureOf(eab).logPure(error, _ => ignore)
        _ <- pureOf(eab).logPure(error, _ => ignore)
        _ <- pureOf(eab).logPure(error, _ => ignore)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.fill(4)(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogOptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), debug)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), info)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), warn)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), error)
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogPureOptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), debug)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), info)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), warn)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), error)
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogOptionTFAIgnoreEmpty: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(ignore, debug)
        _ <- OptionT(effectOf(oa)).log(ignore, info)
        _ <- OptionT(effectOf(oa)).log(ignore, warn)
        _ <- OptionT(effectOf(oa)).log(ignore, error)
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogPureOptionTFAIgnoreEmpty: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(ignore, debug)
        _ <- OptionT(pureOf(oa)).logPure(ignore, info)
        _ <- OptionT(pureOf(oa)).logPure(ignore, warn)
        _ <- OptionT(pureOf(oa)).logPure(ignore, error)
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector(logMsg),
            infoMessages = Vector(logMsg),
            warnMessages = Vector(logMsg),
            errorMessages = Vector(logMsg)
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogOptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogPureOptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
      } yield ()).value

      val expected = logMsg match {
        case Some(logMsg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(ifEmptyMsg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogEitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogPureEitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(error, b => debug(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(error, b => info(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(error, b => warn(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(error, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector(n.toString),
            infoMessages = Vector(n.toString),
            warnMessages = Vector(n.toString),
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogPureEitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => debug(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => info(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => warn(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector(n.toString),
            infoMessages = Vector(n.toString),
            warnMessages = Vector(n.toString),
            errorMessages = Vector(n.toString)
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogPureEitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      val expected = eab match {
        case Right(n) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector.empty
          )

        case Left(msg) =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(msg)
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import loggerf.future.instances.logFuture
        runLog[Future](eab)
      }

      logger ==== expected
    }
  }
}
