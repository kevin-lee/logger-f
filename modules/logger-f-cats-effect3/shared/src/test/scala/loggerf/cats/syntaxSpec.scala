package loggerf.cats

import cats._
import cats.data.{EitherT, OptionT}
import cats.effect._
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.syntax.all._
import extras.hedgehog.ce3.syntax.runner._
import hedgehog._
import hedgehog.runner._
import loggerf.core.Log
import loggerf.logger.LoggerForTesting
import loggerf.syntax.all._

/** @author Kevin Lee
  * @since 2020-04-12
  */
object syntaxSpec extends Properties {
  override def tests: List[Test] = List(
    property("test log(F[A])", testLogFA),
    property("test log(F[Option[A]])", testLogFOptionA),
    property("test log(F[Option[A]])(ignore, message)", testLogFOptionAIgnoreEmpty),
    property("test log(F[Option[A]])(message, ignore)", testLogFOptionAIgnoreSome),
    property("test log(F[Either[A, B]])", testLogFEitherAB),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreLeft),
    property("test log(F[Either[A, B]])(ignore, message)", testLogFEitherABIgnoreRight),
    property("test log(OptionT[F, A])", testLogOptionTFA),
    property("test log(OptionT[F, A])(ignore, message)", testLogOptionTFAIgnoreEmpty),
    property("test log(OptionT[F, A])(message, ignore)", testLogOptionTFAIgnoreSome),
    property("test log(EitherT[F, A, B])", testLogEitherTFAB),
    property("test log(EitherT[F, A, B])(ignore, message)", testLogEitherTFABIgnoreLeft),
    property("test log(EitherT[F, A, B])(message, ignore)", testLogEitherTFABIgnoreRight),
  ) ++ List(
    property("test F[A].log", LogExtensionSpec.testLogFA),
    property("test F[Option[A]].log", LogExtensionSpec.testLogFOptionA),
    property("test F[Option[A]].log(ignore, message)", LogExtensionSpec.testLogFOptionAIgnoreEmpty),
    property("test F[Option[A]].log(message, ignore)", LogExtensionSpec.testLogFOptionAIgnoreSome),
    property("test F[Either[A, B]].log", LogExtensionSpec.testLogFEitherAB),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testLogFEitherABIgnoreLeft),
    property("test F[Either[A, B]].log(ignore, message)", LogExtensionSpec.testLogFEitherABIgnoreRight),
    property("test OptionT[F, A].log", LogExtensionSpec.testLogOptionTFA),
    property("test OptionT[F, A].log(ignore, message)", LogExtensionSpec.testLogOptionTFAIgnoreEmpty),
    property("test OptionT[F, A].log(message, ignore)", LogExtensionSpec.testLogOptionTFAIgnoreSome),
    property("test EitherT[F, A, B].log", LogExtensionSpec.testLogEitherTFAB),
    property("test EitherT[F, A, B].log(ignore, message)", LogExtensionSpec.testLogEitherTFABIgnoreLeft),
    property("test EitherT[F, A, B].log(message, ignore)", LogExtensionSpec.testLogEitherTFABIgnoreRight),
  )

  def testLogFA: Property =
    for {
      debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
      infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
      warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
      errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log]: F[Unit] =
        for {
          _ <- log(effectOf(debugMsg))(debug)
          _ <- log(effectOf(infoMsg))(info)
          _ <- log(effectOf(warnMsg))(warn)
          _ <- log(effectOf(errorMsg))(error)
        } yield ()

      val expected = LoggerForTesting(
        debugMessages = Vector(debugMsg),
        infoMessages = Vector(infoMsg),
        warnMessages = Vector(warnMsg),
        errorMessages = Vector(errorMsg),
      )

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO].completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogFOptionA: Property =
    for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](logMsg).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogFOptionAIgnoreEmpty: Property =
    for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- log(effectOf(oa))(ignore, debug)
          _ <- log(effectOf(oa))(ignore, info)
          _ <- log(effectOf(oa))(ignore, warn)
          _ <- log(effectOf(oa))(ignore, error)
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](logMsg).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogFOptionAIgnoreSome: Property =
    for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
        (for {
          _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
          _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
          _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
          _ <- log(effectOf(oa))(error(ifEmptyMsg), _ => ignore)
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](logMsg).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogFEitherAB: Property =
    for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](eab).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogFEitherABIgnoreLeft: Property =
    for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](eab).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogFEitherABIgnoreRight: Property =
    for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](eab).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogOptionTFA: Property =
    for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](logMsg).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogOptionTFAIgnoreEmpty: Property =
    for {
      logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](logMsg).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogOptionTFAIgnoreSome: Property =
    for {
      logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](logMsg).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogEitherTFAB: Property =
    for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](eab).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogEitherTFABIgnoreLeft: Property =
    for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](eab).completeThen { _ =>
        logger ==== expected
      }
    }

  def testLogEitherTFABIgnoreRight: Property =
    for {
      rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
      leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
      isRight    <- Gen.boolean.log("isRight")
    } yield withIO { implicit ticker =>

      implicit val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
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

      import effectie.instances.ce3.fx.ioFx
      import loggerf.instances.cats.logF
      runLog[IO](eab).completeThen { _ =>
        logger ==== expected
      }
    }

  object LogExtensionSpec {
    def testLogFA: Property =
      for {
        debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
        infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
        warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
        errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log]: F[Unit] =
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO].completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogFOptionA: Property =
      for {
        logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
        ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](logMsg).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogFOptionAIgnoreEmpty: Property =
      for {
        logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
          (for {
            _ <- effectOf(oa).log(ignore, debug)
            _ <- effectOf(oa).log(ignore, info)
            _ <- effectOf(oa).log(ignore, warn)
            _ <- effectOf(oa).log(ignore, error)
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](logMsg).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogFOptionAIgnoreSome: Property =
      for {
        logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
        ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] =
          (for {
            _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
            _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
            _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
            _ <- effectOf(oa).log(error(ifEmptyMsg), _ => ignore)
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](logMsg).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogFEitherAB: Property =
      for {
        rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
        leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
        isRight    <- Gen.boolean.log("isRight")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](eab).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogFEitherABIgnoreLeft: Property =
      for {
        rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
        leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
        isRight    <- Gen.boolean.log("isRight")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](eab).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogFEitherABIgnoreRight: Property =
      for {
        rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
        leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
        isRight    <- Gen.boolean.log("isRight")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](eab).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogOptionTFA: Property =
      for {
        logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
        ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](logMsg).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogOptionTFAIgnoreEmpty: Property =
      for {
        logMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](logMsg).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogOptionTFAIgnoreSome: Property =
      for {
        logMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).option.log("logMsg")
        ifEmptyMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map("[Empty] " + _).log("ifEmptyMsg")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](oa: Option[String]): F[Option[Unit]] = (for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](logMsg).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogEitherTFAB: Property =
      for {
        rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
        leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
        isRight    <- Gen.boolean.log("isRight")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](eab).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogEitherTFABIgnoreLeft: Property =
      for {
        rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
        leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
        isRight    <- Gen.boolean.log("isRight")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](eab).completeThen { _ =>
          logger ==== expected
        }
      }

    def testLogEitherTFABIgnoreRight: Property =
      for {
        rightInt   <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("rightInt")
        leftString <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("leftString")
        isRight    <- Gen.boolean.log("isRight")
      } yield withIO { implicit ticker =>

        implicit val logger: LoggerForTesting = LoggerForTesting()

        def runLog[F[*]: FxCtor: Monad: Log](eab: Either[String, Int]): F[Either[String, Unit]] = (for {
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

        import effectie.instances.ce3.fx.ioFx
        import loggerf.instances.cats.logF
        runLog[IO](eab).completeThen { _ =>
          logger ==== expected
        }
      }

  }
}
