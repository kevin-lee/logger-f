package loggerf

/** @author Kevin Lee
  * @since 2020-04-10
  */
sealed trait LogMessage

object LogMessage {
  sealed trait MaybeIgnorable
  sealed trait Ignorable extends MaybeIgnorable
  sealed trait NotIgnorable extends MaybeIgnorable

  final case class LeveledMessage(message: String, level: Level) extends LogMessage with NotIgnorable
  case object Ignore extends LogMessage with Ignorable
}
