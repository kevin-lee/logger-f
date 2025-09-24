package loggerf

/** @author Kevin Lee
  * @since 2020-04-10
  */
final case class LeveledMessage(message: () => String, throwable: Option[Throwable], level: Level)
object LeveledMessage {

  trait Leveled {
    def level: Level

    def toLazyInput(message: => String): LeveledMessage
  }

  final class StringToLeveledMessage(override val level: Level) extends (String => LeveledMessage) with Leveled {
    override def apply(message: String): LeveledMessage = LeveledMessage(() => message, None, level)

    override def toLazyInput(message: => String): LeveledMessage = LeveledMessage(() => message, None, level)
  }
  object StringToLeveledMessage {
    def apply(level: Level): (String => LeveledMessage) with Leveled = new StringToLeveledMessage(level)
  }

  final class StringToLeveledMessageWithThrowable(override val level: Level, val throwable: Throwable)
      extends (String => LeveledMessage)
      with Leveled {
    override def apply(message: String): LeveledMessage =
      LeveledMessage(() => message, Some(throwable), level)

    def toLazyInput(message: => String): LeveledMessage = LeveledMessage(() => message, Some(throwable), level)
  }
  object StringToLeveledMessageWithThrowable {
    def apply(level: Level)(throwable: Throwable): (String => LeveledMessage) with Leveled =
      new StringToLeveledMessageWithThrowable(level, throwable)
  }

  final class PreprocessedStringToLeveledMessage(override val level: Level, preprocess: String => String)
      extends (String => LeveledMessage)
      with Leveled {
    override def apply(message: String): LeveledMessage = LeveledMessage(() => preprocess(message), None, level)

    override def toLazyInput(message: => String): LeveledMessage =
      LeveledMessage(() => preprocess(message), None, level)
  }

  object PreprocessedStringToLeveledMessage {
    def apply(level: Level, preprocess: String => String): (String => LeveledMessage) with Leveled =
      new PreprocessedStringToLeveledMessage(level, preprocess)
  }
}
case object Ignore
