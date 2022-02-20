package loggerf.cats

import cats._
import cats.syntax.all._
import effectie.core.FxCtor
import effectie.syntax.all._
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.core.syntax.all._
import loggerf.logger.LoggerForTesting

/** @author Kevin Lee
  * @since 2022-02-20
  */
object showSpec extends Properties {
  override def tests: List[Prop] = List(
    property("test LeveledLogMessage with cats.Show", testLeveledLogMessageWithCatsShow)
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

    import loggerf.cats.show.showToLog

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

}
