package loggerf.core.syntax

import loggerf.core.ToLog

import scala.annotation.nowarn

trait LogMessageSyntax {

  import loggerf.LogMessage._
  import loggerf.{Level, LogMessage}

  val debug: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.debug)

  val info: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.info)

  val warn: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.warn)

  val error: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.error)

  def debugA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.debug)

  def infoA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.info)

  def warnA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.warn)

  def errorA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.error)

  def ignore: LogMessage with Ignorable = Ignore

  @nowarn(value = "msg=parameter [\\w\\s]+ in method ignoreA is never used")
  def ignoreA[A](a: A): LogMessage with Ignorable = ignore

}

object LogMessageSyntax extends LogMessageSyntax
