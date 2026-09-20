package loggerf.core.syntax

import loggerf.core.ToLog
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.Level
import loggerf.SourceLocation

trait LogMessageSyntax {

  def debug(using sourceLocation: SourceLocation): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.debug, sourceLocation)

  def debug(throwable: Throwable)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.debug, throwable, sourceLocation)

  def info(using sourceLocation: SourceLocation): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.info, sourceLocation)

  def info(throwable: Throwable)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.info, throwable, sourceLocation)

  def warn(using sourceLocation: SourceLocation): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.warn, sourceLocation)

  def warn(throwable: Throwable)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.warn, throwable, sourceLocation)

  def error(using sourceLocation: SourceLocation): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessage(Level.error, sourceLocation)

  def error(throwable: Throwable)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    LeveledMessage.StringToLeveledMessageWithThrowable(Level.error, throwable, sourceLocation)

  def debugA[A: ToLog](a: A)(using sourceLocation: SourceLocation): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.debug, sourceLocation)

  def debugA[A: ToLog](throwable: Throwable)(using sourceLocation: SourceLocation): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.debug, sourceLocation)

  def infoA[A: ToLog](a: A)(using sourceLocation: SourceLocation): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.info, sourceLocation)

  def infoA[A: ToLog](throwable: Throwable)(using sourceLocation: SourceLocation): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.info, sourceLocation)

  def warnA[A: ToLog](a: A)(using sourceLocation: SourceLocation): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.warn, sourceLocation)

  def warnA[A: ToLog](throwable: Throwable)(using sourceLocation: SourceLocation): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.warn, sourceLocation)

  def errorA[A: ToLog](a: A)(using sourceLocation: SourceLocation): LeveledMessage =
    LeveledMessage(() => ToLog[A].toLogMessage(a), None, Level.error, sourceLocation)

  def errorA[A: ToLog](throwable: Throwable)(using sourceLocation: SourceLocation): A => LeveledMessage =
    (a: A) => LeveledMessage(() => ToLog[A].toLogMessage(a), Some(throwable), Level.error, sourceLocation)

  def ignore: Ignore.type = Ignore

  def ignoreA[A](a: => A): Ignore.type = ignore

}

object LogMessageSyntax extends LogMessageSyntax
