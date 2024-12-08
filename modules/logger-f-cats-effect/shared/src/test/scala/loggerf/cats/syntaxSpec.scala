package loggerf.cats

import cats._
import cats.data.{EitherT, OptionT}
import cats.effect._
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.syntax.all._
import hedgehog._
import hedgehog.runner._
import loggerf.core.Log
import loggerf.syntax.all._
import loggerf.logger._

/** @author Kevin Lee
  * @since 2020-04-12
  */
object syntaxSpec extends Properties {
  override def tests: List[Test] = List(
    property("test log(F[A])", testLogFA),
    property("test log(F[Option[A]])", testLogFOptionA),
    property("test log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test log(F[Option[A]])(message, ignoreA)", testLogFOptionAIgnoreASome),
    property("test log(F[Either[A, B]])", testLogFEitherAB),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test log(F[Either[A, B]])(ignoreA, message)", testLogFEitherABIgnoreALeft),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
    property("test log(F[Either[A, B]])(ignoreA, message)", testLogFEitherABIgnoreARight),
    property("test log(OptionT[F, A])", testLogOptionTFA),
    property("test log(OptionT[F, A])(ignore, message)", testLogOptionTFAIgnoreEmpty),
    property("test log(OptionT[F, A])(message, ignore)", testLogOptionTFAIgnoreSome),
    property("test log(OptionT[F, A])(message, ignoreA)", testLogOptionTFAIgnoreASome),
    property("test log(EitherT[F, A, B])", testLogEitherTFAB),
    property("test log(EitherT[F, A, B])(ignore, message)", testLogEitherTFABIgnoreLeft),
    property("test log(EitherT[F, A, B])(ignoreA, message)", testLogEitherTFABIgnoreALeft),
    property("test log(EitherT[F, A, B])(message, ignore)", testLogEitherTFABIgnoreRight),
    property("test log(EitherT[F, A, B])(message, ignoreA)", testLogEitherTFABIgnoreARight),
  ) ++
    List(
      property("test F[A].log", LogExtensionSpec.testLogFA),
      property("test F[Option[A]].log", LogExtensionSpec.testLogFOptionA),
      property("test F[Option[A]].log(ignore, message)", LogExtensionSpec.testLogFOptionAIgnoreEmpty),
      property("test F[Option[A]].log(message, ignore)", LogExtensionSpec.testLogFOptionAIgnoreSome),
      property("test F[Option[A]].log(message, ignoreA)", LogExtensionSpec.testLogFOptionAIgnoreASome),
      property("test F[Either[A, B]].log", LogExtensionSpec.testLogFEitherAB),
      property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testLogFEitherABIgnoreLeft),
      property("test F[Either[A, B]].log(ignoreA, message)", LogExtensionSpec.testLogFEitherABIgnoreALeft),
      property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testLogFEitherABIgnoreRight),
      property("test F[Either[A, B]].log(ignoreA, message)", LogExtensionSpec.testLogFEitherABIgnoreARight),
      property("test OptionT[F, A].log", LogExtensionSpec.testLogOptionTFA),
      property("test OptionT[F, A].log(ignore, message)", LogExtensionSpec.testLogOptionTFAIgnoreEmpty),
      property("test OptionT[F, A].log(message, ignore)", LogExtensionSpec.testLogOptionTFAIgnoreSome),
      property("test OptionT[F, A].log(message, ignoreA)", LogExtensionSpec.testLogOptionTFAIgnoreASome),
      property("test EitherT[F, A, B].log", LogExtensionSpec.testLogEitherTFAB),
      property("test EitherT[F, A, B].log(ignore, message)", LogExtensionSpec.testLogEitherTFABIgnoreLeft),
      property("test EitherT[F, A, B].log(ignoreA, message)", LogExtensionSpec.testLogEitherTFABIgnoreALeft),
      property("test EitherT[F, A, B].log(message, ignore)", LogExtensionSpec.testLogEitherTFABIgnoreRight),
      property("test EitherT[F, A, B].log(message, ignoreA)", LogExtensionSpec.testLogEitherTFABIgnoreARight),
    )

  private[syntaxSpec] def testLogFA: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log]: F[Unit] =
      (for {
        _ <- log(effectOf(debugMsg))(debug)
        _ <- log(effectOf(infoMsg))(info)
        _ <- log(effectOf(warnMsg))(warn)
        _ <- log(effectOf(errorMsg))(error)
      } yield ())

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    runLog[IO].unsafeRunSync()

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    logger ==== expected
  }

  private[syntaxSpec] def testLogFOptionA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), debug)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), info)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), warn)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), error)
      } yield ().some)

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFOptionAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(ignore, debug)
        _ <- log(effectOf(oa))(ignore, info)
        _ <- log(effectOf(oa))(ignore, warn)
        _ <- log(effectOf(oa))(ignore, error)
      } yield ().some)

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFOptionAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
      } yield ().some)

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFOptionAIgnoreASome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
      (for {
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
        _ <- log(effectOf(oa))(error(ifEmptyMsg), ignoreA)
      } yield ().some)

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFEitherAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, b => debug(b.toString))
      _ <- log(effectOf(eab))(error, b => info(b.toString))
      _ <- log(effectOf(eab))(error, b => warn(b.toString))
      _ <- log(effectOf(eab))(error, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFEitherABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(_ => ignore, b => debug(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => info(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => warn(b.toString))
      _ <- log(effectOf(eab))(_ => ignore, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFEitherABIgnoreALeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(ignoreA, b => debug(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => info(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => warn(b.toString))
      _ <- log(effectOf(eab))(ignoreA, b => error(b.toString))
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFEitherABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
      _ <- log(effectOf(eab))(error, _ => ignore)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogFEitherABIgnoreARight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
      _ <- log(effectOf(eab))(error, ignoreA)
    } yield ().asRight[String]

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogOptionTFA: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), debug)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), info)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), warn)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), error)
    } yield ()).value

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogOptionTFAIgnoreEmpty: Property = for {
    logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(ignore, debug)
      _ <- log(OptionT(effectOf(oa)))(ignore, info)
      _ <- log(OptionT(effectOf(oa)))(ignore, warn)
      _ <- log(OptionT(effectOf(oa)))(ignore, error)
    } yield ()).value

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogOptionTFAIgnoreSome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), _ => ignore)
    } yield ()).value

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogOptionTFAIgnoreASome: Property = for {
    logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
      _ <- log(OptionT(effectOf(oa)))(error(ifEmptyMsg), ignoreA)
    } yield ()).value

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](logMsg).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogEitherTFAB: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(error, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogEitherTFABIgnoreLeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(_ => ignore, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogEitherTFABIgnoreALeft: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => debug(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => info(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => warn(b.toString))
      _ <- log(EitherT(effectOf(eab)))(ignoreA, b => error(b.toString))
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogEitherTFABIgnoreRight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
      _ <- log(EitherT(effectOf(eab)))(error, _ => ignore)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

    logger ==== expected
  }

  private[syntaxSpec] def testLogEitherTFABIgnoreARight: Property = for {
    rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
    leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
    isRight    <- Gen.boolean.log("isRight")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
      _ <- log(EitherT(effectOf(eab)))(error, ignoreA)
    } yield ()).value

    val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

    import effectie.instances.ce2.fx.ioFx
    import loggerf.instances.cats.logF
    val _ = runLog[IO](eab).unsafeRunSync()

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

      def runLog[F[*]: FxCtor: Monad: Log]: F[Unit] =
        (for {
          _ <- effectOf(debugMsg).log(debug)
          _ <- effectOf(infoMsg).log(info)
          _ <- effectOf(warnMsg).log(warn)
          _ <- effectOf(errorMsg).log(error)
        } yield ())

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO].unsafeRunSync()

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg),
      )

      logger ==== expected
    }

    private[syntaxSpec] def testLogFOptionA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), debug)
          _ <- effectOf(oa).log(error(ifEmptyMsg), info)
          _ <- effectOf(oa).log(error(ifEmptyMsg), warn)
          _ <- effectOf(oa).log(error(ifEmptyMsg), error)
        } yield ().some)

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFOptionAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(ignore, debug)
          _ <- effectOf(oa).log(ignore, info)
          _ <- effectOf(oa).log(ignore, warn)
          _ <- effectOf(oa).log(ignore, error)
        } yield ().some)

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFOptionAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
          _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
        } yield ().some)

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFOptionAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
          _ <- effectOf(oa).log(error(ifEmptyMsg), ignoreA)
        } yield ().some)

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFEitherAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, b => debug(b.toString))
        _ <- effectOf(eab).log(error, b => info(b.toString))
        _ <- effectOf(eab).log(error, b => warn(b.toString))
        _ <- effectOf(eab).log(error, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFEitherABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(_ => ignore, b => debug(b.toString))
        _ <- effectOf(eab).log(_ => ignore, b => info(b.toString))
        _ <- effectOf(eab).log(_ => ignore, b => warn(b.toString))
        _ <- effectOf(eab).log(_ => ignore, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFEitherABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(ignoreA, b => debug(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => info(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => warn(b.toString))
        _ <- effectOf(eab).log(ignoreA, b => error(b.toString))
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFEitherABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
        _ <- effectOf(eab).log(error, _ => ignore)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogFEitherABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
        _ <- effectOf(eab).log(error, ignoreA)
      } yield ().asRight[String]

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogOptionTFA: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), debug)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), info)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), warn)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), error)
      } yield ()).value

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogOptionTFAIgnoreEmpty: Property = for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(ignore, debug)
        _ <- OptionT(effectOf(oa)).log(ignore, info)
        _ <- OptionT(effectOf(oa)).log(ignore, warn)
        _ <- OptionT(effectOf(oa)).log(ignore, error)
      } yield ()).value

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogOptionTFAIgnoreSome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), _ => ignore)
      } yield ()).value

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogOptionTFAIgnoreASome: Property = for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
        _ <- OptionT(effectOf(oa)).log(error(ifEmptyMsg), ignoreA)
      } yield ()).value

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](logMsg).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogEitherTFAB: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(error, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(error, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(error, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogEitherTFABIgnoreLeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(_ => ignore, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogEitherTFABIgnoreALeft: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => debug(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => info(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => warn(b.toString))
        _ <- EitherT(effectOf(eab)).log(ignoreA, b => error(b.toString))
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogEitherTFABIgnoreRight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
        _ <- EitherT(effectOf(eab)).log(error, _ => ignore)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

    private[syntaxSpec] def testLogEitherTFABIgnoreARight: Property = for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield {

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
        _ <- EitherT(effectOf(eab)).log(error, ignoreA)
      } yield ()).value

      val eab = if (isRight) rightInt.asRight[String] else leftString.asLeft[Int]

      import effectie.instances.ce2.fx.ioFx
      import loggerf.instances.cats.logF
      val _ = runLog[IO](eab).unsafeRunSync()

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

      logger ==== expected
    }

  }
}
