package loggerf.instances

import _root_.cats._
import _root_.cats.syntax.all._
import effectie.core.FxCtor
import effectie.syntax.all._
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.instances.show.showToLog
import loggerf.logger.LoggerForTesting
import loggerf.syntax.all._

/** @author Kevin Lee
  * @since 2022-02-20
  */
object showSpec extends Properties {
  override def tests: List[Prop] = List(
    property("test LeveledLogMessage with cats.Show", testLeveledLogMessageWithCatsShow),
    property("test LeveledLogMessage with cats.Show with prefix", testLeveledLogMessageWithCatsShowWithPrefix),
  )

  final case class Something(message: String)
  object Something {
    implicit val somethingShow: Show[Something] = something => s"Something(message=${something.message})"
  }

  def testLeveledLogMessageWithCatsShow: Property = for {
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("debugMsg")
    infoMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("infoMsg")
    warnMsg  <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("warnMsg")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("errorMsg")
  } yield {

    val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[*]: Log: FxCtor: Monad]: F[Unit] =
      for {
        _ <- log(pureOf(debugMsg))(debugA)
        _ <- log(pureOf(infoMsg))(infoA)
        _ <- log(pureOf(warnMsg))(warnA)
        _ <- log(pureOf(errorMsg))(errorA)
      } yield ()

    import LogForTesting.{FxCtorForTesting, Identity}
    implicit val lgForTesting: Log[Identity] = LogForTesting(logger)

    val expected = LoggerForTesting(
      debugMessages = Vector(debugMsg.show),
      infoMessages = Vector(infoMsg.show),
      warnMessages = Vector(warnMsg.show),
      errorMessages = Vector(errorMsg.show)
    )

    runLog[Identity]
    logger ==== expected
  }

  def testLeveledLogMessageWithCatsShowWithPrefix: Property =
    for {
      prefixString <- Gen.string(Gen.unicode, Range.linear(5, 20)).log("prefixString")
      debugMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("debugMsg")
      infoMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("infoMsg")
      warnMsg      <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("warnMsg")
      errorMsg     <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(Something(_)).log("errorMsg")
    } yield {

      val logger: LoggerForTesting = LoggerForTesting()

      def runLog[F[*]: Log: FxCtor: Monad]: F[Unit] =
        for {
          _ <- log(pureOf(debugMsg))(debugAWith(prefix(prefixString)))
          _ <- log(pureOf(infoMsg))(infoAWith(prefix(prefixString)))
          _ <- log(pureOf(warnMsg))(warnAWith(prefix(prefixString)))
          _ <- log(pureOf(errorMsg))(errorAWith(prefix(prefixString)))
        } yield ()

      import LogForTesting.{FxCtorForTesting, Identity}
      implicit val lgForTesting: Log[Identity] = LogForTesting(logger)

      val expected = LoggerForTesting(
        debugMessages = Vector(prefixString + debugMsg.show),
        infoMessages = Vector(prefixString + infoMsg.show),
        warnMessages = Vector(prefixString + warnMsg.show),
        errorMessages = Vector(prefixString + errorMsg.show)
      )

      runLog[Identity]
      logger ==== expected
    }

}
