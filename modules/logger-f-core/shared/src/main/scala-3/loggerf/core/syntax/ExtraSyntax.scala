package loggerf.core.syntax

import loggerf.core.syntax.ExtraSyntax.Prefix

/** @author Kevin Lee
  * @since 2022-10-29
  */
trait ExtraSyntax {

  import loggerf.LeveledMessage._
  import loggerf.{Level, LeveledMessage}

  def prefix(pre: String): Prefix =
    Prefix(message => pre + message)

  inline private def debug0(f: String => String): String => LeveledMessage =
    message => LeveledMessage(f(message), Level.debug)

  inline private def info0(f: String => String): String => LeveledMessage =
    message => LeveledMessage(f(message), Level.info)

  inline private def warn0(f: String => String): String => LeveledMessage =
    message => LeveledMessage(f(message), Level.warn)

  inline private def error0(f: String => String): String => LeveledMessage =
    message => LeveledMessage(f(message), Level.error)

  def debug(prefix: Prefix): String => LeveledMessage =
    debug0(prefix.value)

  def info(prefix: Prefix): String => LeveledMessage =
    info0(prefix.value)

  def warn(prefix: Prefix): String => LeveledMessage =
    warn0(prefix.value)

  def error(prefix: Prefix): String => LeveledMessage =
    error0(prefix.value)

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
