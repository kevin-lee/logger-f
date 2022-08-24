package loggerf.core.syntax

import loggerf.core.ToLog

trait LogMessageSyntax {

  import loggerf.LogMessage._
  import loggerf.{Level, LogMessage}

  def debug(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.debug)

  def info(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.info)

  def warn(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.warn)

  def error(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.error)

  def debugA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.debug)

  def infoA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.info)

  def warnA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.warn)

  def errorA[A: ToLog](a: A): LogMessage with NotIgnorable =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.error)

  def ignore: LogMessage with Ignorable = Ignore

  def ignoreA[A](a: A): LogMessage with Ignorable = ignore

}

object LogMessageSyntax extends LogMessageSyntax
