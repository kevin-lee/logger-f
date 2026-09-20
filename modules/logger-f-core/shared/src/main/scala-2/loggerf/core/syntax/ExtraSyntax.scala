package loggerf.core.syntax

import loggerf.LogMessage.LeveledMessage.PreprocessedStringToLeveledMessage
import loggerf.SourceLocation

/** @author Kevin Lee
  * @since 2022-10-29
  */
trait ExtraSyntax {

  import loggerf.LogMessage._
  import loggerf.core.syntax.ExtraSyntax._
  import loggerf.{Level, LogMessage}

  def prefix(pre: => String): Prefix =
    new Prefix(message => pre + message)

  @inline private[ExtraSyntax] def debug0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.debug, f, sourceLocation)

  @inline private[ExtraSyntax] def info0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.info, f, sourceLocation)

  @inline private[ExtraSyntax] def warn0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.warn, f, sourceLocation)

  @inline private[ExtraSyntax] def error0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.error, f, sourceLocation)

  // format: off
  def debug(prefix: Prefix): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.debugWithPrefix

  def info(prefix: Prefix): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.infoWithPrefix

  def warn(prefix: Prefix): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.warnWithPrefix

  def error(prefix: Prefix): (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled = macro LogMessageSyntaxMacro.errorWithPrefix
  // format: on

  import loggerf.core.ToLog

  def debugAWith[A: ToLog](prefix: Prefix)(implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    a => debug0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

  def infoAWith[A: ToLog](prefix: Prefix)(implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    a => info0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

  def warnAWith[A: ToLog](prefix: Prefix)(implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    a => warn0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

  def errorAWith[A: ToLog](prefix: Prefix)(implicit sourceLocation: SourceLocation): A => LogMessage with NotIgnorable =
    a => error0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

}

object ExtraSyntax extends ExtraSyntax {
  final class Prefix(val value: String => String) extends AnyVal
}
