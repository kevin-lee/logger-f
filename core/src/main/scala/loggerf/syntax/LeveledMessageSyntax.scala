package loggerf.syntax

trait LeveledMessageSyntax {

  import loggerf.LeveledMessage._
  import loggerf.{Level, LeveledMessage}

  def debug(message: String): LeveledMessage with NotIgnorable =
    LogMessage(message, Level.debug)

  def info(message: String): LeveledMessage with NotIgnorable =
    LogMessage(message, Level.info)

  def warn(message: String): LeveledMessage with NotIgnorable =
    LogMessage(message, Level.warn)

  def error(message: String): LeveledMessage with NotIgnorable =
    LogMessage(message, Level.error)

  def ignore: LeveledMessage with MaybeIgnorable = Ignore

}
