package loggerf.instances

import _root_.cats.Monad
import _root_.cats.syntax.either._
import _root_.cats.syntax.flatMap._
import _root_.cats.syntax.functor._
import _root_.cats.syntax.option._
import effectie.core.Fx
import extras.concurrent.testing.types.ErrorLogger
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.core.syntax.all._
import loggerf.instances.tries.logTry
import loggerf.logger.LoggerForTesting

import scala.util.Try

/** @author Kevin Lee
  * @since 2023-09-05
  */
object triesSpec extends Properties {
  override def tests: List[Test] = List(
    property("test Log.log(F[A])", testLogFA),
    property("test Log.log(F[Option[A]])", testLogFOptionA),
    property("test Log.log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test Log.log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test Log.log(F[Either[A, B]])", testLogFEitherAB),
    property("test Log.log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test Log.log(F[Either[A, B]])(message, ignore)", testLogFEitherABIgnoreRight),
    property("test Log.log_(F[A])", testLog_FA),
    property("test Log.log_(F[Option[A]])", testLog_FOptionA),
    property("test Log.log_(F[Option[A]])(ignore, message)", testLog_FOptionAIgnoreEmpty),
    property("test Log.log_(F[Option[A]])(message, ignore)", testLog_FOptionAIgnoreSome),
    property("test Log.log_(F[Either[A, B]])", testLog_FEitherAB),
    property("test Log.log_(F[Either[A, B]])(ignore, message)", testLog_FEitherABIgnoreLeft),
    property("test Log.log_(F[Either[A, B]])(message, ignore)", testLog_FEitherABIgnoreRight),
  )

  implicit val errorLogger: ErrorLogger[Throwable] = ErrorLogger.printlnDefaultErrorLogger

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
      errorMessages = Vector(errorMsg),
    )

    import effectie.instances.tries.fx._

    val _ = runLog[Try]

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](logMsg)

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](logMsg)

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](logMsg)

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](eab)

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](eab)

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](eab)

    logger ==== expected
  }

  def testLog_FA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad]: F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(debugMsg))(debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(infoMsg))(info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(warnMsg))(warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(errorMsg))(error) }

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    import effectie.instances.tries.fx._
    val _ = runLog[Try]

    logger ==== expected

  }

  def testLog_FOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), error) }

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](logMsg)

    logger ==== expected
  }

  def testLog_FOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(ignore, debug)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, info) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, warn) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(ignore, error) }

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](logMsg)

    logger ==== expected
  }

  def testLog_FOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Fx: Log: Monad](oa: Option[String]): F[Unit] =
      Log[F]
        .log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }
        .flatMap { _ => Log[F].log_(Fx[F].effectOf(oa))(error(ifEmptyMsg), _ => ignore) }

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](logMsg)

    logger ==== expected
  }

  def testLog_FEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](eab)

    logger ==== expected
  }

  def testLog_FEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](eab)

    logger ==== expected
  }

  def testLog_FEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

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

    import effectie.instances.tries.fx._
    val _ = runLog[Try](eab)

    logger ==== expected
  }

  ///

}
