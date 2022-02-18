package loggerf.syntax

import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.Level

trait LeveledMessageSyntax {

  def debug(message: String): LeveledMessage =
    LeveledMessage(message, Level.debug)

  def info(message: String): LeveledMessage =
    LeveledMessage(message, Level.info)

  def warn(message: String): LeveledMessage =
    LeveledMessage(message, Level.warn)

  def error(message: String): LeveledMessage =
    LeveledMessage(message, Level.error)

  def ignore: Ignore.type = Ignore

}
