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
import loggerf.cats.syntax.all._
import loggerf.logger._

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
    property("test log(F[Option[A]])(message, ignoreA)", testLogFOptionAIgnoreASome),
    property("test logPure(F[Option[A]])(message, ignore)", testLogPureFOptionAIgnoreSome),
    property("test logPure(F[Option[A]])(message, ignoreA)", testLogPureFOptionAIgnoreASome),
    property("test log(F[Either[A, B]])", testLogFEitherAB),
    property("test logPure(F[Either[A, B]])", testLogPureFEitherAB),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test log(F[Either[A, B]])(ignoreA, message)", testLogFEitherABIgnoreALeft),
    property("test logPure(F[Either[A, B]])(ignore, message)", testLogPureFEitherABIgnoreLeft),
    property("test logPure(F[Either[A, B]])(ignoreA, message)", testLogPureFEitherABIgnoreALeft),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
    property("test log(F[Either[A, B]])(ignoreA, message)", testLogFEitherABIgnoreARight),
    property("test logPure(F[Either[A, B]])(ignore, message)", testLogPureFEitherABIgnoreRight),
    property("test logPure(F[Either[A, B]])(ignoreA, message)", testLogPureFEitherABIgnoreARight),
    property("test log(OptionT[F, A])", testLogOptionTFA),
    property("test logPure(OptionT[F, A])", testLogPureOptionTFA),
    property("test log(OptionT[F, A])(ignore, message)", testLogOptionTFAIgnoreEmpty),
    property("test logPure(OptionT[F, A])(ignore, message)", testLogPureOptionTFAIgnoreEmpty),
    property("test log(OptionT[F, A])(message, ignore)", testLogOptionTFAIgnoreSome),
    property("test log(OptionT[F, A])(message, ignoreA)", testLogOptionTFAIgnoreASome),
    property("test logPure(OptionT[F, A])(message, ignore)", testLogPureOptionTFAIgnoreSome),
    property("test logPure(OptionT[F, A])(message, ignoreA)", testLogPureOptionTFAIgnoreASome),
    property("test log(EitherT[F, A, B])", testLogEitherTFAB),
    property("test logPure(EitherT[F, A, B])", testLogPureEitherTFAB),
    property("test log(EitherT[F, A, B])(ignore, message)", testLogEitherTFABIgnoreLeft),
    property("test log(EitherT[F, A, B])(ignoreA, message)", testLogEitherTFABIgnoreALeft),
    property("test logPure(EitherT[F, A, B])(ignore, message)", testLogPureEitherTFABIgnoreLeft),
    property("test logPure(EitherT[F, A, B])(ignoreA, message)", testLogPureEitherTFABIgnoreALeft),
    property("test log(EitherT[F, A, B])(message, ignore)", testLogEitherTFABIgnoreRight),
    property("test log(EitherT[F, A, B])(message, ignoreA)", testLogEitherTFABIgnoreARight),
    property("test logPure(EitherT[F, A, B])(message, ignore)", testLogPureEitherTFABIgnoreRight),
    property("test logPure(EitherT[F, A, B])(message, ignoreA)", testLogPureEitherTFABIgnoreARight),
  ) ++
    List(
      property("test F[A].log", LogExtensionSpec.testLogFA),
      property("test F[A].logPure", LogExtensionSpec.testLogPureFA),
      property("test F[Option[A]].log", LogExtensionSpec.testLogFOptionA),
      property("test F[Option[A]].logPure", LogExtensionSpec.testLogPureFOptionA),
      property("test F[Option[A]].log(ignore, message)", LogExtensionSpec.testLogFOptionAIgnoreEmpty),
      property("test F[Option[A]].logPure(ignore, message)", LogExtensionSpec.testLogPureFOptionAIgnoreEmpty),
      property("test F[Option[A]].log(message, ignore)", LogExtensionSpec.testLogFOptionAIgnoreSome),
      property("test F[Option[A]].log(message, ignoreA)", LogExtensionSpec.testLogFOptionAIgnoreASome),
      property("test F[Option[A]].logPure(message, ignore)", LogExtensionSpec.testLogPureFOptionAIgnoreSome),
      property("test F[Option[A]].logPure(message, ignoreA)", LogExtensionSpec.testLogPureFOptionAIgnoreASome),
      property("test F[Either[A, B]].log", LogExtensionSpec.testLogFEitherAB),
      property("test F[Either[A, B]].logPure", LogExtensionSpec.testLogPureFEitherAB),
      property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testLogFEitherABIgnoreLeft),
      property("test F[Either[A, B]].log(ignoreA, message)", LogExtensionSpec.testLogFEitherABIgnoreALeft),
      property("test F[Either[A, B]].logPure(ignore, message)", LogExtensionSpec.testLogPureFEitherABIgnoreLeft),
      property("test F[Either[A, B]].logPure(ignoreA, message)", LogExtensionSpec.testLogPureFEitherABIgnoreALeft),
      property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testLogFEitherABIgnoreRight),
      property("test F[Either[A, B]].log(ignoreA, message)", LogExtensionSpec.testLogFEitherABIgnoreARight),
      property("test F[Either[A, B]].logPure(ignore, message)", LogExtensionSpec.testLogPureFEitherABIgnoreRight),
      property("test F[Either[A, B]].logPure(ignoreA, message)", LogExtensionSpec.testLogPureFEitherABIgnoreARight),
      property("test OptionT[F, A].log", LogExtensionSpec.testLogOptionTFA),
      property("test OptionT[F, A].logPure", LogExtensionSpec.testLogPureOptionTFA),
      property("test OptionT[F, A].log(ignore, message)", LogExtensionSpec.testLogOptionTFAIgnoreEmpty),
      property("test OptionT[F, A].logPure(ignore, message)", LogExtensionSpec.testLogPureOptionTFAIgnoreEmpty),
      property("test OptionT[F, A].log(message, ignore)", LogExtensionSpec.testLogOptionTFAIgnoreSome),
      property("test OptionT[F, A].log(message, ignoreA)", LogExtensionSpec.testLogOptionTFAIgnoreASome),
      property("test OptionT[F, A].logPure(message, ignore)", LogExtensionSpec.testLogPureOptionTFAIgnoreSome),
      property("test OptionT[F, A].logPure(message, ignoreA)", LogExtensionSpec.testLogPureOptionTFAIgnoreASome),
      property("test EitherT[F, A, B].log", LogExtensionSpec.testLogEitherTFAB),
      property("test EitherT[F, A, B].logPure", LogExtensionSpec.testLogPureEitherTFAB),
      property("test EitherT[F, A, B].log(ignore, message)", LogExtensionSpec.testLogEitherTFABIgnoreLeft),
      property("test EitherT[F, A, B].log(ignoreA, message)", LogExtensionSpec.testLogEitherTFABIgnoreALeft),
      property("test EitherT[F, A, B].logPure(ignore, message)", LogExtensionSpec.testLogPureEitherTFABIgnoreLeft),
      property("test EitherT[F, A, B].logPure(ignoreA, message)", LogExtensionSpec.testLogPureEitherTFABIgnoreALeft),
      property("test EitherT[F, A, B].log(message, ignore)", LogExtensionSpec.testLogEitherTFABIgnoreRight),
      property("test EitherT[F, A, B].log(message, ignoreA)", LogExtensionSpec.testLogEitherTFABIgnoreARight),
      property("test EitherT[F, A, B].logPure(message, ignore)", LogExtensionSpec.testLogPureEitherTFABIgnoreRight),
      property("test EitherT[F, A, B].logPure(message, ignoreA)", LogExtensionSpec.testLogPureEitherTFABIgnoreARight)
    )

  private[syntaxSpec] def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad]: F[Unit] =
      (for {
        _ <- log(effectOf(debugMsg))(debug)
        _ <- log(effectOf(infoMsg))(info)
        _ <- log(effectOf(warnMsg))(warn)
        _ <- log(effectOf(errorMsg))(error)
      } yield ())

    import effectie.ce2.fx.ioFx
    runLog[IO].unsafeRunSync()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg)
    )

    logger ==== expected
  }

  private[syntaxSpec] def testLogPureFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad]: F[Unit] =
      (for {
        _ <- logPure(pureOf(debugMsg))(debug)
        _ <- logPure(pureOf(infoMsg))(info)
        _ <- logPure(pureOf(warnMsg))(warn)
        _ <- logPure(pureOf(errorMsg))(error)
      } yield ())

    import effectie.ce2.fx.ioFx
    runLog[IO].unsafeRunSync()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg)
    )

    logger ==== expected
  }

  private[syntaxSpec] def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), debug)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), info)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), warn)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(ignore, debug)
        _ <- log(effectOf(oa))(ignore, info)
        _ <- log(effectOf(oa))(ignore, warn)
        _ <- log(effectOf(oa))(ignore, error)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(ignore, debug)
        _ <- logPure(pureOf(oa))(ignore, info)
        _ <- logPure(pureOf(oa))(ignore, warn)
        _ <- logPure(pureOf(oa))(ignore, error)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFOptionAIgnoreASome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFOptionAIgnoreASome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- logPure(pureOf(oa))(error(ifEmptyMsg), ignoreA)
      } yield ().some)

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, b => debug(b.toString))
      _ <- log(effectOf(eab))(error, b => info(b.toString))
      _ <- log(effectOf(eab))(error, b => warn(b.toString))
      _ <- log(effectOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(error, b => info(b.toString))
      _ <- logPure(pureOf(eab))(error, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFEitherABIgnoreALeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(ignoreA, b => debug(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => info(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => warn(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => info(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFEitherABIgnoreALeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(ignoreA, b => debug(b.toString))
      _ <- logPure(pureOf(eab))(ignoreA, b => info(b.toString))
      _ <- logPure(pureOf(eab))(ignoreA, b => warn(b.toString))
      _ <- logPure(pureOf(eab))(ignoreA, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogFEitherABIgnoreARight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
      _ <- logPure(pureOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureFEitherABIgnoreARight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- logPure(pureOf(eab))(error, ignoreA)
      _ <- logPure(pureOf(eab))(error, ignoreA)
      _ <- logPure(pureOf(eab))(error, ignoreA)
      _ <- logPure(pureOf(eab))(error, ignoreA)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), debug)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), info)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), warn)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogOptionTFAIgnoreEmpty: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log(OptionT(effectOf(oa)))(ignore, info)
      _ <- log(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log(OptionT(effectOf(oa)))(ignore, error)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureOptionTFAIgnoreEmpty: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(ignore, debug)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, info)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, warn)
      _ <- logPure(OptionT(pureOf(oa)))(ignore, error)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogOptionTFAIgnoreASome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureOptionTFAIgnoreASome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- logPure(OptionT(pureOf(oa)))(error(ifEmptyMsg), ignoreA)
    } yield ()).value

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogEitherTFABIgnoreALeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(_ => ignore, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureEitherTFABIgnoreALeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(ignoreA, b => debug(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(ignoreA, b => info(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(ignoreA, b => warn(b.toString))
      _ <- logPure(EitherT(pureOf(eab)))(ignoreA, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogEitherTFABIgnoreARight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
      _ <- logPure(EitherT(pureOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  private[syntaxSpec] def testLogPureEitherTFABIgnoreARight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- logPure(EitherT(pureOf(eab)))(error, ignoreA)
      _ <- logPure(EitherT(pureOf(eab)))(error, ignoreA)
      _ <- logPure(EitherT(pureOf(eab)))(error, ignoreA)
      _ <- logPure(EitherT(pureOf(eab)))(error, ignoreA)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.ce2.fx.ioFx
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

  object LogExtensionSpec {
    private[syntaxSpec] def testLogFA: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad]: F[Unit] =
        (for {
          _ <- effectOf(debugMsg).log(debug)
          _ <- effectOf(infoMsg).log(info)
          _ <- effectOf(warnMsg).log(warn)
          _ <- effectOf(errorMsg).log(error)
        } yield ())

      import effectie.ce2.fx.ioFx
      runLog[IO].unsafeRunSync()

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg)
      )

      logger ==== expected
    }

    private[syntaxSpec] def testLogPureFA: Property = for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad]: F[Unit] =
        (for {
          _ <- pureOf(debugMsg).logPure(debug)
          _ <- pureOf(infoMsg).logPure(info)
          _ <- pureOf(warnMsg).logPure(warn)
          _ <- pureOf(errorMsg).logPure(error)
        } yield ())

      import effectie.ce2.fx.ioFx
      runLog[IO].unsafeRunSync()

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg)
      )

      logger ==== expected
    }

    private[syntaxSpec] def testLogFOptionA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), debug)
          _ <- effectOf(oa).log(error(ifEmptyMsg), info)
          _ <- effectOf(oa).log(error(ifEmptyMsg), warn)
          _ <- effectOf(oa).log(error(ifEmptyMsg), error)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFOptionA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), debug)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), info)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), warn)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), error)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFOptionAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(ignore, debug)
          _ <- effectOf(oa).log(ignore, info)
          _ <- effectOf(oa).log(ignore, warn)
          _ <- effectOf(oa).log(ignore, error)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFOptionAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- pureOf(oa).logPure(ignore, debug)
          _ <- pureOf(oa).logPure(ignore, info)
          _ <- pureOf(oa).logPure(ignore, warn)
          _ <- pureOf(oa).logPure(ignore, error)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFOptionAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFOptionAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFOptionAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), _ => ignore)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFOptionAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), ignoreA)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), ignoreA)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), ignoreA)
          _ <- pureOf(oa).logPure(error(ifEmptyMsg), ignoreA)
        } yield ().some)

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFEitherAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, b => debug(b.toString))
        _ <- effectOf(eab).log(error, b => info(b.toString))
        _ <- effectOf(eab).log(error, b => warn(b.toString))
        _ <- effectOf(eab).log(error, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFEitherAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(error, b => debug(b.toString))
        _ <- pureOf(eab).logPure(error, b => info(b.toString))
        _ <- pureOf(eab).logPure(error, b => warn(b.toString))
        _ <- pureOf(eab).logPure(error, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFEitherABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(_ => ignore, b => debug(b.toString))
        _ <- effectOf(eab).log(_ => ignore, b => info(b.toString))
        _ <- effectOf(eab).log(_ => ignore, b => warn(b.toString))
        _ <- effectOf(eab).log(_ => ignore, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFEitherABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(ignoreA, b => debug(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => info(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => warn(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFEitherABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(_ => ignore, b => debug(b.toString))
        _ <- pureOf(eab).logPure(_ => ignore, b => info(b.toString))
        _ <- pureOf(eab).logPure(_ => ignore, b => warn(b.toString))
        _ <- pureOf(eab).logPure(_ => ignore, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFEitherABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(ignoreA, b => debug(b.toString))
        _ <- pureOf(eab).logPure(ignoreA, b => info(b.toString))
        _ <- pureOf(eab).logPure(ignoreA, b => warn(b.toString))
        _ <- pureOf(eab).logPure(ignoreA, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFEitherABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogFEitherABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFEitherABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(error, _ => ignore)
        _ <- pureOf(eab).logPure(error, _ => ignore)
        _ <- pureOf(eab).logPure(error, _ => ignore)
        _ <- pureOf(eab).logPure(error, _ => ignore)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureFEitherABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- pureOf(eab).logPure(error, ignoreA)
        _ <- pureOf(eab).logPure(error, ignoreA)
        _ <- pureOf(eab).logPure(error, ignoreA)
        _ <- pureOf(eab).logPure(error, ignoreA)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogOptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), debug)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), info)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), warn)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), error)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureOptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), debug)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), info)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), warn)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), error)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogOptionTFAIgnoreEmpty: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(ignore, debug)
        _ <- OptionT(effectOf(oa)).log(ignore, info)
        _ <- OptionT(effectOf(oa)).log(ignore, warn)
        _ <- OptionT(effectOf(oa)).log(ignore, error)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureOptionTFAIgnoreEmpty: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(ignore, debug)
        _ <- OptionT(pureOf(oa)).logPure(ignore, info)
        _ <- OptionT(pureOf(oa)).logPure(ignore, warn)
        _ <- OptionT(pureOf(oa)).logPure(ignore, error)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogOptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogOptionTFAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureOptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), _ => ignore)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureOptionTFAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(pureOf(oa)).logPure(error(ifEmptyMsg), ignoreA)
      } yield ()).value

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogEitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(error, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(error, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(error, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureEitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(error, b => debug(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(error, b => info(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(error, b => warn(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(error, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogEitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogEitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureEitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => debug(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => info(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => warn(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(_ => ignore, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureEitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(ignoreA, b => debug(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(ignoreA, b => info(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(ignoreA, b => warn(b.toString))
        _ <- EitherT(pureOf(eab)).logPure(ignoreA, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogEitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogEitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureEitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
        _ <- EitherT(pureOf(eab)).logPure(error, _ => ignore)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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

    private[syntaxSpec] def testLogPureEitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(pureOf(eab)).logPure(error, ignoreA)
        _ <- EitherT(pureOf(eab)).logPure(error, ignoreA)
        _ <- EitherT(pureOf(eab)).logPure(error, ignoreA)
        _ <- EitherT(pureOf(eab)).logPure(error, ignoreA)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.ce2.fx.ioFx
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
}
