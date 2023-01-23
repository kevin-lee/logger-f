package loggerf.core.syntax

import loggerf.core.ToLog
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.Level

trait LogMessageSyntax {

  val debug: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.debug)

  val info: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.info)

  val warn: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.warn)

  val error: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.error)

  def debugA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.debug)

  def infoA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.info)

  def warnA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.warn)

  def errorA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), Level.error)

  def ignore: Ignore.type = Ignore

  def ignoreA[A](a: => A): Ignore.type = ignore

}

object LogMessageSyntax extends LogMessageSyntax
