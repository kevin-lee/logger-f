package loggerf

/** @author Kevin Lee
  * @since 2020-04-10
  */
final case class LeveledMessage(
  message: () => String,
  throwable: Option[Throwable],
  level: Level,
  sourceLocation: SourceLocation,
)
object LeveledMessage {

  trait Leveled {
    def level: Level

    def sourceLocation: SourceLocation

    def toLazyInput(message: => String): LeveledMessage
  }

  final class StringToLeveledMessage(override val level: Level, override val sourceLocation: SourceLocation)
      extends (String => LeveledMessage)
      with Leveled {
    override def apply(message: String): LeveledMessage = LeveledMessage(() => message, None, level, sourceLocation)

    override def toLazyInput(message: => String): LeveledMessage =
      LeveledMessage(() => message, None, level, sourceLocation)
  }
  object StringToLeveledMessage {
    def apply(level: Level, sourceLocation: SourceLocation): (String => LeveledMessage) with Leveled =
      new StringToLeveledMessage(level, sourceLocation)
  }

  final class StringToLeveledMessageWithThrowable(
    override val level: Level,
    val throwable: Throwable,
    override val sourceLocation: SourceLocation,
  ) extends (String => LeveledMessage)
      with Leveled {
    override def apply(message: String): LeveledMessage =
      LeveledMessage(() => message, Some(throwable), level, sourceLocation)

    def toLazyInput(message: => String): LeveledMessage =
      LeveledMessage(() => message, Some(throwable), level, sourceLocation)
  }
  object StringToLeveledMessageWithThrowable {
    def apply(
      level: Level,
      throwable: Throwable,
      sourceLocation: SourceLocation,
    ): (String => LeveledMessage) with Leveled =
      new StringToLeveledMessageWithThrowable(level, throwable, sourceLocation)
  }

  final class PreprocessedStringToLeveledMessage(
    override val level: Level,
    preprocess: String => String,
    override val sourceLocation: SourceLocation,
  ) extends (String => LeveledMessage)
      with Leveled {
    override def apply(message: String): LeveledMessage =
      LeveledMessage(() => preprocess(message), None, level, sourceLocation)

    override def toLazyInput(message: => String): LeveledMessage =
      LeveledMessage(() => preprocess(message), None, level, sourceLocation)
  }

  object PreprocessedStringToLeveledMessage {
    def apply(
      level: Level,
      preprocess: String => String,
      sourceLocation: SourceLocation,
    ): (String => LeveledMessage) with Leveled =
      new PreprocessedStringToLeveledMessage(level, preprocess, sourceLocation)
  }
}
case object Ignore
