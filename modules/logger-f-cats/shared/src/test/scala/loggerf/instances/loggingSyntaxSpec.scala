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
object loggingSyntaxSpec extends Properties {
  override def tests: List[Test] = List(
    property("test log(F[A])", LogFASpec.testLogFA),
    property("test log(F[Option[A]])", LogFASpec.testLogFOptionA),
    property("test log(F[Option[A]])(ignore, message)", LogFASpec.testLogFOptionAIgnoreEmpty),
    property("test log(F[Option[A]])(message, ignore)", LogFASpec.testLogFOptionAIgnoreSome),
    property("test log(F[Option[A]])(message, ignoreA)", LogFASpec.testLogFOptionAIgnoreASome),
    property("test log(F[Either[A, B]])", LogFASpec.testLogFEitherAB),
    property("test log(F[Either[A, B]])(ignore, message)", LogFASpec.testLogFEitherABIgnoreLeft),
    property("test log(F[Either[A, B]])(ignoreA, message)", LogFASpec.testLogFEitherABIgnoreALeft),
    property("test log(F[Either[A, B]])(message, ignore)", LogFASpec.testLogFEitherABIgnoreRight),
    property("test log(F[Either[A, B]])(message, ignoreA)", LogFASpec.testLogFEitherABIgnoreARight),
    property("test log(OptionT[F, A])", LogFASpec.testLogOptionTFA),
    property("test log(OptionT[F, A])(ignore, message)", LogFASpec.testLogOptionTFAIgnoreEmpty),
    property("test log(OptionT[F, A])(message, ignore)", LogFASpec.testLogOptionTFAIgnoreSome),
    property("test log(EitherT[F, A, B])", LogFASpec.testLogEitherTFAB),
    property("test log(EitherT[F, A, B])(ignore, message)", LogFASpec.testLogEitherTFABIgnoreLeft),
    property("test log(EitherT[F, A, B])(ignoreA, message)", LogFASpec.testLogEitherTFABIgnoreALeft),
    property("test log(EitherT[F, A, B])(message, ignore)", LogFASpec.testLogEitherTFABIgnoreRight),
    property("test log(EitherT[F, A, B])(message, ignoreA)", LogFASpec.testLogEitherTFABIgnoreARight),
  ) ++ List(
    property("test log_(F[A])", LogFASpec.testLog_FA),
    property("test log_(F[Option[A]])", LogFASpec.testLog_FOptionA),
    property("test log_(F[Option[A]])(ignore, message)", LogFASpec.testLog_FOptionAIgnoreEmpty),
    property("test log_(F[Option[A]])(message, ignore)", LogFASpec.testLog_FOptionAIgnoreSome),
    property("test log_(F[Option[A]])(message, ignoreA)", LogFASpec.testLog_FOptionAIgnoreASome),
    property("test log_(F[Either[A, B]])", LogFASpec.testLogFEitherAB),
    property("test log_(F[Either[A, B]])(ignore, message)", LogFASpec.testLog_FEitherABIgnoreLeft),
    property("test log_(F[Either[A, B]])(ignoreA, message)", LogFASpec.testLog_FEitherABIgnoreALeft),
    property("test log_(F[Either[A, B]])(message, ignore)", LogFASpec.testLog_FEitherABIgnoreRight),
    property("test log_(F[Either[A, B]])(message, ignoreA)", LogFASpec.testLog_FEitherABIgnoreARight),
    property("test log_(OptionT[F, A])", LogFASpec.testLog_OptionTFA),
    property("test log_(OptionT[F, A])(ignore, message)", LogFASpec.testLog_OptionTFAIgnoreEmpty),
    property("test log_(OptionT[F, A])(message, ignore)", LogFASpec.testLog_OptionTFAIgnoreSome),
    property("test log_(EitherT[F, A, B])", LogFASpec.testLog_EitherTFAB),
    property("test log_(EitherT[F, A, B])(ignore, message)", LogFASpec.testLog_EitherTFABIgnoreLeft),
    property("test log_(EitherT[F, A, B])(ignoreA, message)", LogFASpec.testLog_EitherTFABIgnoreALeft),
    property("test log_(EitherT[F, A, B])(message, ignore)", LogFASpec.testLog_EitherTFABIgnoreRight),
    property("test log_(EitherT[F, A, B])(message, ignoreA)", LogFASpec.testLog_EitherTFABIgnoreARight),
  ) ++ List(
    property("test F[A].log", LogExtensionSpec.testFALog),
    property("test F[Option[A]].log", LogExtensionSpec.testFOptionALog),
    property("test F[Option[A]].log(ignore, message)", LogExtensionSpec.testFOptionALogIgnoreEmpty),
    property("test F[Option[A]].log(message, ignore)", LogExtensionSpec.testFOptionALogIgnoreSome),
    property("test F[Either[A, B]].log", LogExtensionSpec.testFEitherABLog),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testFEitherABLogIgnoreLeft),
    property("test F[Either[A, B]].log(ignoreA, message)", LogExtensionSpec.testFEitherABLogIgnoreALeft),
    property("test F[Either[A, B]].log(message, ignore)", LogExtensionSpec.testFEitherABLogIgnoreRight),
    property("test F[Either[A, B]].log(message, ignoreA)", LogExtensionSpec.testFEitherABLogIgnoreARight),
    property("test OptionT[F, A].log", LogExtensionSpec.testLogOptionTFA),
    property("test OptionT[F, A].log(ignore, message)", LogExtensionSpec.testLogOptionTFAIgnoreEmpty),
    property("test OptionT[F, A].log(message, ignore)", LogExtensionSpec.testLogOptionTFAIgnoreSome),
    property("test EitherT[F, A, B].log", LogExtensionSpec.testLogEitherTFAB),
    property("test EitherT[F, A, B].log(ignore, message)", LogExtensionSpec.testLogEitherTFABIgnoreLeft),
    property("test EitherT[F, A, B].log(ignoreA, message)", LogExtensionSpec.testLogEitherTFABIgnoreALeft),
    property("test EitherT[F, A, B].log(message, ignore)", LogExtensionSpec.testLogEitherTFABIgnoreRight),
    property("test EitherT[F, A, B].log(message, ignoreA)", LogExtensionSpec.testLogEitherTFABIgnoreARight),
  ) ++ List(
    property("test F[A].log_", LogExtensionSpec.testFALog_()),
    property("test F[Option[A]].log_", LogExtensionSpec.testFOptionALog_()),
    property("test F[Option[A]].log_(ignore, message)", LogExtensionSpec.testFOptionALog_IgnoreEmpty),
    property("test F[Option[A]].log_(message, ignore)", LogExtensionSpec.testFOptionALog_IgnoreSome),
    property("test F[Either[A, B]].log_", LogExtensionSpec.testFEitherABLog_()),
    property("test F[Either[A, B]].log_(ignore, message)", LogExtensionSpec.testFEitherABLog_IgnoreLeft),
    property("test F[Either[A, B]].log_(ignoreA, message)", LogExtensionSpec.testFEitherABLog_IgnoreALeft),
    property("test F[Either[A, B]].log_(message, ignore)", LogExtensionSpec.testFEitherABLog_IgnoreRight),
    property("test F[Either[A, B]].log_(message, ignoreA)", LogExtensionSpec.testFEitherABLog_IgnoreARight),
    property("test OptionT[F, A].log_", LogExtensionSpec.testLog_OptionTFA),
    property("test OptionT[F, A].log_(ignore, message)", LogExtensionSpec.testLog_OptionTFAIgnoreEmpty),
    property("test OptionT[F, A].log_(message, ignore)", LogExtensionSpec.testLog_OptionTFAIgnoreSome),
    property("test EitherT[F, A, B].log_", LogExtensionSpec.testLog_EitherTFAB),
    property("test EitherT[F, A, B].log_(ignore, message)", LogExtensionSpec.testLog_EitherTFABIgnoreLeft),
    property("test EitherT[F, A, B].log_(ignoreA, message)", LogExtensionSpec.testLog_EitherTFABIgnoreALeft),
    property("test EitherT[F, A, B].log_(message, ignore)", LogExtensionSpec.testLog_EitherTFABIgnoreRight),
    property("test EitherT[F, A, B].log_(message, ignoreA)", LogExtensionSpec.testLog_EitherTFABIgnoreARight),
  )

  implicit val errorLogger: ErrorLogger[Throwable] = ErrorLogger.printlnDefaultErrorLogger

  private val waitFor300Millis = WaitFor(400.milliseconds)

  object LogFASpec {

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
        errorMessages = Vector(errorMsg),
      )

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogFOptionAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
          _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
          _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
          _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogFEitherABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogFEitherABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
            errorMessages = Vector(logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogOptionTFAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }
    // ////

    def testLog_FA: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future]
      }

      logger ==== expected
    }

    def testLog_FOptionA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), debug)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), info)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), warn)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), error)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_FOptionAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- log_(effectOf(oa))(ignore, debug)
          _ <- log_(effectOf(oa))(ignore, info)
          _ <- log_(effectOf(oa))(ignore, warn)
          _ <- log_(effectOf(oa))(ignore, error)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_FOptionAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_FOptionAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
          _ <- log_(effectOf(oa))(error(ifEmptyMsg), ignoreA)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_FEitherAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_FEitherABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_FEitherABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_OptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
      } yield ()).value

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
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_OptionTFAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- log_(OptionT(effectOf(oa)))(ignore, debug)
        _ <- log_(OptionT(effectOf(oa)))(ignore, info)
        _ <- log_(OptionT(effectOf(oa)))(ignore, warn)
        _ <- log_(OptionT(effectOf(oa)))(ignore, error)
      } yield ()).value

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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_OptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
        _ <- log_(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
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
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_EitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }
    // ////

  }

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
        errorMessages = Vector(errorMsg),
      )

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(ignoreA, b => debug(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => info(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => warn(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => error(b.toString))
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLogIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
            errorMessages = Vector(logMsg),
          )

        case None =>
          LoggerForTesting(
            debugMessages = Vector.empty,
            infoMessages = Vector.empty,
            warnMessages = Vector.empty,
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLogOptionTFAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => error(b.toString))
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLogEitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFALog_(): Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad]: F[Unit] =
        for {
          _ <- effectOf(debugMsg).log_(debug)
          _ <- effectOf(infoMsg).log_(info)
          _ <- effectOf(warnMsg).log_(warn)
          _ <- effectOf(errorMsg).log_(error)
        } yield ()

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg),
      )

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future]
      }

      logger ==== expected
    }

    def testFOptionALog_(): Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log_(error(ifEmptyMsg), debug)
          _ <- effectOf(oa).log_(error(ifEmptyMsg), info)
          _ <- effectOf(oa).log_(error(ifEmptyMsg), warn)
          _ <- effectOf(oa).log_(error(ifEmptyMsg), error)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALog_IgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- effectOf(oa).log_(ignore, debug)
          _ <- effectOf(oa).log_(ignore, info)
          _ <- effectOf(oa).log_(ignore, warn)
          _ <- effectOf(oa).log_(ignore, error)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFOptionALog_IgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] =
        for {
          _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log_(error(ifEmptyMsg), _ => ignore)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testFEitherABLog_(): Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log_(error, b => debug(b.toString))
        _ <- effectOf(eab).log_(error, b => info(b.toString))
        _ <- effectOf(eab).log_(error, b => warn(b.toString))
        _ <- effectOf(eab).log_(error, b => error(b.toString))
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log_(_ => ignore, b => debug(b.toString))
        _ <- effectOf(eab).log_(_ => ignore, b => info(b.toString))
        _ <- effectOf(eab).log_(_ => ignore, b => warn(b.toString))
        _ <- effectOf(eab).log_(_ => ignore, b => error(b.toString))
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLog_IgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log_(ignoreA, b => debug(b.toString))
        _ <- effectOf(eab).log_(ignoreA, b => info(b.toString))
        _ <- effectOf(eab).log_(ignoreA, b => warn(b.toString))
        _ <- effectOf(eab).log_(ignoreA, b => error(b.toString))
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
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

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log_(error, _ => ignore)
        _ <- effectOf(eab).log_(error, _ => ignore)
        _ <- effectOf(eab).log_(error, _ => ignore)
        _ <- effectOf(eab).log_(error, _ => ignore)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testFEitherABLog_IgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log_(error, ignoreA)
        _ <- effectOf(eab).log_(error, ignoreA)
        _ <- effectOf(eab).log_(error, ignoreA)
        _ <- effectOf(eab).log_(error, ignoreA)
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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_OptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), debug)
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), info)
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), warn)
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), error)
      } yield ()).value

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
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_OptionTFAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log_(ignore, debug)
        _ <- OptionT(effectOf(oa)).log_(ignore, info)
        _ <- OptionT(effectOf(oa)).log_(ignore, warn)
        _ <- OptionT(effectOf(oa)).log_(ignore, error)
      } yield ()).value

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
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_OptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log_(error(ifEmptyMsg), _ => ignore)
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
            errorMessages = Vector(ifEmptyMsg),
          )
      }

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](logMsg)
      }

      logger ==== expected
    }

    def testLog_EitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log_(error, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log_(error, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log_(error, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log_(error, b => error(b.toString))
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log_(_ => ignore, b => error(b.toString))
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log_(ignoreA, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log_(ignoreA, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log_(ignoreA, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log_(ignoreA, b => error(b.toString))
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log_(error, _ => ignore)
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

    def testLog_EitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Log: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log_(error, ignoreA)
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

      implicit val es: ExecutorService  = ConcurrentSupport.newExecutorService(2)
      implicit val ec: ExecutionContext =
        ConcurrentSupport.newExecutionContextWithLogger(es, ErrorLogger.printlnExecutionContextErrorLogger)

      val _ = ConcurrentSupport.futureToValueAndTerminate(es, waitFor300Millis) {
        import effectie.instances.future.fxCtor._
        import loggerf.instances.cats.logF
        runLog[Future](eab)
      }

      logger ==== expected
    }

  }
}
