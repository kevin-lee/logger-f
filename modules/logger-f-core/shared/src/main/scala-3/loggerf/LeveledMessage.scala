package loggerf

/** @author Kevin Lee
  * @since 2020-04-10
  */
final case class LeveledMessage(message: () => String, level: Level)
object LeveledMessage {

  trait Leveled {
    def level: Level

    def toLazyInput(message: => String): LeveledMessage
  }

  final class StringToLeveledMessage(override val level: Level) extends (String => LeveledMessage) with Leveled {
    override def apply(message: String): LeveledMessage = LeveledMessage(() => message, level)

    override def toLazyInput(message: => String): LeveledMessage = LeveledMessage(() => message, level)
  }
  object StringToLeveledMessage {
    def apply(level: Level): (String => LeveledMessage) with Leveled = new StringToLeveledMessage(level)
  }

}
case object Ignore
