package loggerf.core.syntax

import loggerf.core.ToLog

trait LogMessageSyntax {

  import loggerf.LogMessage._
  import loggerf.{Level, LogMessage}

  val debug: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.debug)

  def debug(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.debug)(throwable)

  val info: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.info)

  def info(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.info)(throwable)

  val warn: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.warn)

  def warn(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.warn)(throwable)

  val error: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.error)

  def error(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.error)(throwable)

  def debugA[A: ToLog]: A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.debug)

  def debugA[A: ToLog](throwable: Throwable): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.debug)

  def infoA[A: ToLog]: A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.info)

  def infoA[A: ToLog](throwable: Throwable): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.info)

  def warnA[A: ToLog]: A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.warn)

  def warnA[A: ToLog](throwable: Throwable): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.warn)

  def errorA[A: ToLog]: A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.error)

  def errorA[A: ToLog](throwable: Throwable): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.error)

  def ignore: LogMessage with Ignorable = Ignore

  def ignoreA[A]: A => LogMessage with Ignorable = (_: A) => ignore

}

object LogMessageSyntax extends LogMessageSyntax
