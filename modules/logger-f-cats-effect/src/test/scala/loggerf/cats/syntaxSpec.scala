package loggerf.cats

import cats._
import cats.data.{EitherT, OptionT}
import cats.effect._
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.syntax.all._
import hedgehog._
import hedgehog.runner._
import loggerf.cats.instances.logF
import loggerf.cats.syntax._
import loggerf.logger._
import loggerf.syntax._

/** @author Kevin Lee
  * @since 2020-04-12
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
    property("test logPure(EitherT[F, A, B])(message, ignore)", testLogPureEitherTFABIgnoreRight)
  )

  def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad]: F[Unit] =
      (for {
        _ <- log(effectOf(debugMsg))(debug)
        _ <- log(effectOf(infoMsg))(info)
        _ <- log(effectOf(warnMsg))(warn)
        _ <- log(effectOf(errorMsg))(error)
      } yield ())

    import effectie.cats.fx.ioFx
    runLog[IO].unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad]: F[Unit] =
      (for {
        _ <- logPure(pureOf(debugMsg))(debug)
        _ <- logPure(pureOf(infoMsg))(info)
        _ <- logPure(pureOf(warnMsg))(warn)
        _ <- logPure(pureOf(errorMsg))(error)
      } yield ())

    import effectie.cats.fx.ioFx
    runLog[IO].unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), debug)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), info)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), warn)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(ignore, debug)
        _ <- log(effectOf(oa))(ignore, info)
        _ <- log(effectOf(oa))(ignore, warn)
        _ <- log(effectOf(oa))(ignore, error)
      } yield ().some)

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(ignore, debug)
        _ <- logPure(pureOf(oa))(ignore, info)
        _ <- logPure(pureOf(oa))(ignore, warn)
        _ <- logPure(pureOf(oa))(ignore, error)
      } yield ().some)

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, b => debug(b.toString))
      _ <- log(effectOf(eab))(error, b => info(b.toString))
      _ <- log(effectOf(eab))(error, b => warn(b.toString))
      _ <- log(effectOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(error, b => info(b.toString))
      _ <- logPure(pureOf(eab))(error, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => info(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

  def testLogOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogPureOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), debug)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), info)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), warn)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogOptionTFAIgnoreEmpty: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log(OptionT(effectOf(oa)))(ignore, info)
      _ <- log(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log(OptionT(effectOf(oa)))(ignore, error)
    } yield ()).value

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

  def testLogPureOptionTFAIgnoreEmpty: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(ignore, debug)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, info)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, warn)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, error)
    } yield ()).value

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

  def testLogOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogPureOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    import effectie.cats.fx.ioFx
    runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogPureEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

  def testLogPureEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

  def testLogEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  def testLogPureEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.cats.fx.ioFx
    runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

}
