package loggerf.core.syntax

import loggerf.core.syntax.ExtraSyntax.Prefix

/** @author Kevin Lee
  * @since 2022-10-29
  */
trait ExtraSyntax {

  import loggerf.LogMessage._
  import loggerf.{Level, LogMessage}

  def prefix(pre: String): Prefix =
    new Prefix(message => pre + message)

  @inline private def debug0(f: String => String): String => LogMessage with NotIgnorable =
    message => LeveledMessage(f(message), Level.debug)

  @inline private def info0(f: String => String): String => LogMessage with NotIgnorable =
    message => LeveledMessage(f(message), Level.info)

  @inline private def warn0(f: String => String): String => LogMessage with NotIgnorable =
    message => LeveledMessage(f(message), Level.warn)

  @inline private def error0(f: String => String): String => LogMessage with NotIgnorable =
    message => LeveledMessage(f(message), Level.error)

  def debug(prefix: Prefix): String => LogMessage with NotIgnorable =
    debug0(prefix.value)

  def info(prefix: Prefix): String => LogMessage with NotIgnorable =
    info0(prefix.value)

  def warn(prefix: Prefix): String => LogMessage with NotIgnorable =
    warn0(prefix.value)

  def error(prefix: Prefix): String => LogMessage with NotIgnorable =
    error0(prefix.value)

}

object ExtraSyntax extends ExtraSyntax {
  final class Prefix(val value: String => String) extends AnyVal
}
