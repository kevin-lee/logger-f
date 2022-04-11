package loggerf.monix

import cats._
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.syntax.all._
import hedgehog._
import hedgehog.runner._
import loggerf.core.Log
import loggerf.logger.LoggerForTesting
import loggerf.cats.instances.logF
import loggerf.cats.syntax.all._
import monix.eval.Task

/** @author Kevin Lee
  * @since 2020-04-12
  */
object instancesSpec extends Properties {
  override def tests: List[Test] = List(
    property("test Log.log(F[A])", testLogFA),
    property("test Log.logPure(F[A])", testLogPureFA),
    property("test Log.log(F[Option[A]])", testLogFOptionA),
    property("test Log.logPure(F[Option[A]])", testLogPureFOptionA),
    property("test Log.log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test Log.logPure(F[Option[A]])(ignore, message)", testLogPureFOptionAIgnoreEmpty),
    property("test Log.log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test Log.logPure(F[Option[A]])(message, ignore)", testLogPureFOptionAIgnoreSome),
    property("test Log.log(F[Either[A, B]])", testLogFEitherAB),
    property("test Log.logPure(F[Either[A, B]])", testLogPureFEitherAB),
    property("test Log.log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test Log.logPure(F[Either[A, B]])(ignore, message)", testLogPureFEitherABIgnoreLeft),
    property("test Log.log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
    property("test Log.logPure(F[Either[A, B]])(ignore, message)", testLogPureFEitherABIgnoreRight),
  )

  import monix.execution.Scheduler.Implicits.global

  def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad]: F[Unit] =
      (for {
        _ <- Log[F].log(effectOf(debugMsg))(debug)
        _ <- Log[F].log(effectOf(infoMsg))(info)
        _ <- Log[F].log(effectOf(warnMsg))(warn)
        _ <- Log[F].log(effectOf(errorMsg))(error)
      } yield ())

    import effectie.monix.fx._
    runLog[Task].runSyncUnsafe()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg)
    )

    logger ==== expected
  }

  def testLogPureFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad]: F[Unit] =
      (for {
        _ <- Log[F].logPure(pureOf(debugMsg))(debug)
        _ <- Log[F].logPure(pureOf(infoMsg))(info)
        _ <- Log[F].logPure(pureOf(warnMsg))(warn)
        _ <- Log[F].logPure(pureOf(errorMsg))(error)
      } yield ())

    import effectie.monix.fx._
    runLog[Task].runSyncUnsafe()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg)
    )

    logger ==== expected
  }

  def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.monix.fx._
    runLog[Task](logMsg).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogPureFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), debug)
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), info)
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), warn)
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.monix.fx._
    runLog[Task](logMsg).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(effectOf(oa))(ignore, debug)
        _ <- Log[F].log(effectOf(oa))(ignore, info)
        _ <- Log[F].log(effectOf(oa))(ignore, warn)
        _ <- Log[F].log(effectOf(oa))(ignore, error)
      } yield ().some)

    import effectie.monix.fx._
    runLog[Task](logMsg).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogPureFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].logPure(pureOf(oa))(ignore, debug)
        _ <- Log[F].logPure(pureOf(oa))(ignore, info)
        _ <- Log[F].logPure(pureOf(oa))(ignore, warn)
        _ <- Log[F].logPure(pureOf(oa))(ignore, error)
      } yield ().some)

    import effectie.monix.fx._
    runLog[Task](logMsg).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.monix.fx._
    runLog[Task](logMsg).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogPureFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- Log[F].logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.monix.fx._
    runLog[Task](logMsg).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(effectOf(eab))(error, b => debug(b.toString))
      _ <- Log[F].log(effectOf(eab))(error, b => info(b.toString))
      _ <- Log[F].log(effectOf(eab))(error, b => warn(b.toString))
      _ <- Log[F].log(effectOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.monix.fx._
    runLog[Task](eab).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogPureFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].logPure(pureOf(eab))(error, b => debug(b.toString))
      _ <- Log[F].logPure(pureOf(eab))(error, b => info(b.toString))
      _ <- Log[F].logPure(pureOf(eab))(error, b => warn(b.toString))
      _ <- Log[F].logPure(pureOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.monix.fx._
    runLog[Task](eab).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- Log[F].log(effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- Log[F].log(effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- Log[F].log(effectOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.monix.fx._
    runLog[Task](eab).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogPureFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].logPure(pureOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- Log[F].logPure(pureOf(eab))(_ => ignore, b => info(b.toString))
      _ <- Log[F].logPure(pureOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- Log[F].logPure(pureOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.monix.fx._
    runLog[Task](eab).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].log(effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(effectOf(eab))(error, _ => ignore)
      _ <- Log[F].log(effectOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.monix.fx._
    runLog[Task](eab).runSyncUnsafe()

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

    logger ==== expected
  }

  def testLogPureFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- Log[F].logPure(pureOf(eab))(error, _ => ignore)
      _ <- Log[F].logPure(pureOf(eab))(error, _ => ignore)
      _ <- Log[F].logPure(pureOf(eab))(error, _ => ignore)
      _ <- Log[F].logPure(pureOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.monix.fx._
    runLog[Task](eab).runSyncUnsafe()

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

    logger ==== expected
  }

}