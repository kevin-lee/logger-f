package loggerf.core.syntax

import loggerf.SourceLocation
import loggerf.core.ToLog

trait LogMessageSyntax {

  import loggerf.LogMessage._
  import loggerf.{Level, LogMessage}

  // format: off
  def debug: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.debug

  def debug(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.debugWithThrowable

  def info: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.info

  def info(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.infoWithThrowable

  def warn: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.warn

  def warn(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.warnWithThrowable

  def error: (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.error

  def error(throwable: Throwable): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.errorWithThrowable
  // format: on

  def debugA[A: ToLog](implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.debug, sourceLocation)

  def debugA[A: ToLog](throwable: Throwable)(
    implicit sourceLocation: SourceLocation
  ): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.debug, sourceLocation)

  def infoA[A: ToLog](implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.info, sourceLocation)

  def infoA[A: ToLog](throwable: Throwable)(
    implicit sourceLocation: SourceLocation
  ): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.info, sourceLocation)

  def warnA[A: ToLog](implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.warn, sourceLocation)

  def warnA[A: ToLog](throwable: Throwable)(
    implicit sourceLocation: SourceLocation
  ): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.warn, sourceLocation)

  def errorA[A: ToLog](implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.error, sourceLocation)

  def errorA[A: ToLog](throwable: Throwable)(
    implicit sourceLocation: SourceLocation
  ): A => LogMessage with NotIgnorable =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.error, sourceLocation)

  def ignore: LogMessage with Ignorable = Ignore

  def ignoreA[A]: A => LogMessage with Ignorable = (_: A) => ignore

}

object LogMessageSyntax extends LogMessageSyntax
