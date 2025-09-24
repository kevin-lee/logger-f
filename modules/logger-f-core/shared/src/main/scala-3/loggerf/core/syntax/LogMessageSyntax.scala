package loggerf.core.syntax

import loggerf.core.ToLog
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.Level

trait LogMessageSyntax {

  val debug: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.debug)

  def debug(throwable: Throwable): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.debug)(throwable)

  val info: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.info)

  def info(throwable: Throwable): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.info)(throwable)

  val warn: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.warn)

  def warn(throwable: Throwable): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.warn)(throwable)

  val error: (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.error)

  def error(throwable: Throwable): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.error)(throwable)

  def debugA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.debug)

  def debugA[A: ToLog](throwable: Throwable): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.debug)

  def infoA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.info)

  def infoA[A: ToLog](throwable: Throwable): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.info)

  def warnA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.warn)

  def warnA[A: ToLog](throwable: Throwable): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.warn)

  def errorA[A: ToLog](a: A): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.error)

  def errorA[A: ToLog](throwable: Throwable): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.error)

  def ignore: Ignore.type = Ignore

  def ignoreA[A](a: => A): Ignore.type = ignore

}

object LogMessageSyntax extends LogMessageSyntax
