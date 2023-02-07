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

  final class PreprocessedStringToLeveledMessage(override val level: Level, preprocess: String => String)
      extends (String => LeveledMessage)
      with Leveled {
    override def apply(message: String): LeveledMessage = LeveledMessage(() => preprocess(message), level)

    override def toLazyInput(message: => String): LeveledMessage = LeveledMessage(() => preprocess(message), level)
  }

  object PreprocessedStringToLeveledMessage {
    def apply(level: Level, preprocess: String => String): (String => LeveledMessage) with Leveled =
      new PreprocessedStringToLeveledMessage(level, preprocess)
  }
}
case object Ignore
