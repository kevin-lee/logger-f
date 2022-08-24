package loggerf.core.syntax

import loggerf.core.ToLog
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.Level

trait LogMessageSyntax {

  def debug(message: String): LeveledMessage =
    LeveledMessage(message, Level.debug)

  def info(message: String): LeveledMessage =
    LeveledMessage(message, Level.info)

  def warn(message: String): LeveledMessage =
    LeveledMessage(message, Level.warn)

  def error(message: String): LeveledMessage =
    LeveledMessage(message, Level.error)

  def debugA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.debug)

  def infoA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.info)

  def warnA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.warn)

  def errorA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(ToLog[A].toLogMessage(a), Level.error)

  def ignore: Ignore.type = Ignore

  def ignoreA[A](a: A): Ignore.type = ignore

}

object LogMessageSyntax extends LogMessageSyntax
