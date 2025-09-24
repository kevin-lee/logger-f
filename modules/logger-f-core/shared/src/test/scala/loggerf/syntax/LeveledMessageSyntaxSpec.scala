package loggerf.syntax

import cats._
import cats.syntax.all._
import effectie.core._
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.core.syntax.all._
import loggerf.logger.LoggerForTesting
import loggerf.test_data.Something

/** @author Kevin Lee
  * @since 2022-02-19
  */
object LeveledMessageSyntaxSpec extends Properties {
  override def tests: List[Prop] = List(
    property("test LeveledLogMessage", testLeveledLogMessage),
    property("test LeveledLogMessage with Throwable", testLeveledLogMessageWithThrowable),
    property("test LeveledLogMessage with ToLog", testLeveledLogMessageWithToLog),
  )

  def testLeveledLogMessage: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {
    val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: FxCtor: Monad]: F[Unit] =
      for {
        _ <- log(FxCtor[F].pureOf(debugMsg))(debug)
        _ <- log(FxCtor[F].pureOf(infoMsg))(info)
        _ <- log(FxCtor[F].pureOf(warnMsg))(warn)
        _ <- log(FxCtor[F].pureOf(errorMsg))(error)
      } yield ()

    import LogForTesting.{FxCtorForTesting, Identity}
    implicit val lgForTesting: Log[Identity] = LogForTesting(logger)

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg),
      infoMessages = Vector(infoMsg),
      warnMessages = Vector(warnMsg),
      errorMessages = Vector(errorMsg),
    )

    runLog[Identity]
    logger ==== expected
  }

  def testLeveledLogMessageWithThrowable: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {
    val logger: LoggerForTesting = LoggerForTesting()

    val debugThrowable = new RuntimeException("test DEBUG Throwable")
    val infoThrowable  = new RuntimeException("test INFO Throwable")
    val warnThrowable  = new RuntimeException("test WARN Throwable")
    val errorThrowable = new RuntimeException("test ERROR Throwable")

    def runLog[F[*]: Log: FxCtor: Monad]: F[Unit] =
      for {
        _ <- log(FxCtor[F].pureOf(debugMsg))(debug)
        _ <- log(FxCtor[F].pureOf(debugMsg))(debug(debugThrowable))
        _ <- log(FxCtor[F].pureOf(infoMsg))(info)
        _ <- log(FxCtor[F].pureOf(infoMsg))(info(infoThrowable))
        _ <- log(FxCtor[F].pureOf(warnMsg))(warn)
        _ <- log(FxCtor[F].pureOf(warnMsg))(warn(warnThrowable))
        _ <- log(FxCtor[F].pureOf(errorMsg))(error)
        _ <- log(FxCtor[F].pureOf(errorMsg))(error(errorThrowable))
      } yield ()

    import LogForTesting.{FxCtorForTesting, Identity}
    implicit val lgForTesting: Log[Identity] = LogForTesting(logger)

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg, s"$debugMsg\n${debugThrowable.toString}"),
      infoMessages = Vector(infoMsg, s"$infoMsg\n${infoThrowable.toString}"),
      warnMessages = Vector(warnMsg, s"$warnMsg\n${warnThrowable.toString}"),
      errorMessages = Vector(errorMsg, s"$errorMsg\n${errorThrowable.toString}"),
    )

    runLog[Identity]
    logger ==== expected
  }

  def testLeveledLogMessageWithToLog: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("errorMsg")
  } yield {

    val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: FxCtor: Monad]: F[Unit] =
      for {
        _ <- log(FxCtor[F].pureOf(debugMsg))(debugA)
        _ <- log(FxCtor[F].pureOf(infoMsg))(infoA)
        _ <- log(FxCtor[F].pureOf(warnMsg))(warnA)
        _ <- log(FxCtor[F].pureOf(errorMsg))(errorA)
      } yield ()

    import LogForTesting.{FxCtorForTesting, Identity}
    implicit val lgForTesting: Log[Identity] = LogForTesting(logger)

    val expected = LoggerForTesting(
      debugMessages = Vector(ToLog[Something].toLogMessage(debugMsg)),
      infoMessages = Vector(ToLog[Something].toLogMessage(infoMsg)),
      warnMessages = Vector(ToLog[Something].toLogMessage(warnMsg)),
      errorMessages = Vector(ToLog[Something].toLogMessage(errorMsg)),
    )

    runLog[Identity]
    logger ==== expected
  }

}
