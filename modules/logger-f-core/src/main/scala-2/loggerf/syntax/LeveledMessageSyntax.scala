package loggerf.syntax

trait LeveledMessageSyntax {

  import loggerf.LogMessage._
  import loggerf.{Level, LogMessage}

  def debug(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.debug)

  def info(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.info)

  def warn(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.warn)

  def error(message: String): LogMessage with NotIgnorable =
    LeveledMessage(message, Level.error)

  def ignore: LogMessage with Ignorable = Ignore

}
