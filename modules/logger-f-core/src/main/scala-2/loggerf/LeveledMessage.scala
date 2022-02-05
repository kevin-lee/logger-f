package loggerf

/** @author Kevin Lee
  * @since 2020-04-10
  */
sealed trait LeveledMessage

object LeveledMessage {
  sealed trait MaybeIgnorable
  sealed trait Ignorable extends MaybeIgnorable
  sealed trait NotIgnorable extends MaybeIgnorable

  final case class LogMessage(message: String, level: Level) extends LeveledMessage with NotIgnorable
  case object Ignore extends LeveledMessage with Ignorable
}
