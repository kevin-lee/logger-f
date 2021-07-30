package loggerf

/**
 * @author Kevin Lee
 * @since 2020-04-10
 */
enum LeveledMessage {
  case LogMessage(message: String, level: Level) extends LeveledMessage with LeveledMessage.NotIgnorable
  case Ignore extends LeveledMessage with LeveledMessage.Ignorable
}

object LeveledMessage {
  sealed trait MaybeIgnorable
  sealed trait Ignorable extends MaybeIgnorable
  sealed trait NotIgnorable extends MaybeIgnorable
}