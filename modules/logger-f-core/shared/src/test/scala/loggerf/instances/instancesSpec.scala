package loggerf.instances

import cats.Monad
import cats.syntax.all._
import effectie.core._
import extras.concurrent.testing.ConcurrentSupport
import extras.concurrent.testing.types.{ErrorLogger, WaitFor}
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.core.syntax.all._
import loggerf.instances.future.logFuture
import loggerf.logger._

import java.util.concurrent.ExecutorService
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
object instancesSpec extends Properties {
  override def tests: List[Test] = List(
    property("test Log.log(F[A])", testLogFA),
    property("test Log.log(F[Option[A]])", testLogFOptionA),
    property("test Log.log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test Log.log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test Log.log(F[Either[A, B]])", testLogFEitherAB),
    property("test Log.log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test Log.log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
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

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(debugMsg))(debug)
        _ <- Log[F].log(Fx[F].effectOf(infoMsg))(info)
        _ <- Log[F].log(Fx[F].effectOf(warnMsg))(warn)
        _ <- Log[F].log(Fx[F].effectOf(errorMsg))(error)
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

      runLog[Future]

    }
    logger ==== expected

  }

  def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), info)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), error)
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
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, debug)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, info)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, warn)
        _ <- Log[F].log(Fx[F].effectOf(oa))(ignore, error)
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
          errorMessages = Vector.empty
        )
    }

    implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
    implicit val ec: ExecutionContext =
      ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

    ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
      runLog[Future](logMsg)
    }

    logger ==== expected
  }

  def testLogFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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

    def runLog[F[*]: Fx: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(Fx[F].effectOf(eab))(error, _ => ignore)
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
      runLog[Future](eab)
    }

    logger ==== expected
  }

}