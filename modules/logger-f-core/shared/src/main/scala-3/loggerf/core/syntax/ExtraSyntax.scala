package loggerf.core.syntax

import loggerf.core.syntax.ExtraSyntax.Prefix
import loggerf.LeveledMessage.PreprocessedStringToLeveledMessage
import loggerf.SourceLocation

/** @author Kevin Lee
  * @since 2022-10-29
  */
trait ExtraSyntax {

  import loggerf.{Level, LeveledMessage}

  def prefix(pre: => String): Prefix =
    Prefix(message => pre + message)

  inline private def debug0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.debug, f, sourceLocation)

  inline private def info0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.info, f, sourceLocation)

  inline private def warn0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.warn, f, sourceLocation)

  inline private def error0(
    f: String => String,
    sourceLocation: SourceLocation,
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    PreprocessedStringToLeveledMessage(Level.error, f, sourceLocation)

  def debug(prefix: Prefix)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    debug0(prefix.value, sourceLocation)

  def info(prefix: Prefix)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    info0(prefix.value, sourceLocation)

  def warn(prefix: Prefix)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    warn0(prefix.value, sourceLocation)

  def error(prefix: Prefix)(
    using sourceLocation: SourceLocation
  ): (String => LeveledMessage) with LeveledMessage.Leveled =
    error0(prefix.value, sourceLocation)

  import loggerf.core.ToLog

  def debugAWith[A: ToLog](prefix: Prefix)(using sourceLocation: SourceLocation): A => LeveledMessage =
    a => debug0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

  def infoAWith[A: ToLog](prefix: Prefix)(using sourceLocation: SourceLocation): A => LeveledMessage =
    a => info0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

  def warnAWith[A: ToLog](prefix: Prefix)(using sourceLocation: SourceLocation): A => LeveledMessage =
    a => warn0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

  def errorAWith[A: ToLog](prefix: Prefix)(using sourceLocation: SourceLocation): A => LeveledMessage =
    a => error0(prefix.value, sourceLocation)(ToLog[A].toLogMessage(a))

}

object ExtraSyntax extends ExtraSyntax {

  type Prefix = Prefix.Prefix
  object Prefix {
    opaque type Prefix = String => String
    def apply(prefix: String => String): Prefix = prefix

    extension (prefix: Prefix) {
      def value: String => String = prefix
    }
  }

}
