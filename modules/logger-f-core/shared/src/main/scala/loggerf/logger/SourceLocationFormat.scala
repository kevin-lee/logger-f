package loggerf.logger

import loggerf.SourceLocation

/** Tells how the location-aware `CanLog` methods put a [[loggerf.SourceLocation]] into the log message.
  *
  * The rendered location is `prefix + sourceLocation.render + suffix`, and it goes before or after the message as
  * `position` tells. Nothing is added implicitly, so an empty `prefix` or `suffix` means no separator at all.
  *
  * {{{
  * SourceLocationFormat.default.render("Hello", location)
  * // Hello at com.example.Foo.bar(Foo.scala:42)
  *
  * SourceLocationFormat.beforeMessage("[", "] ").render("Hello", location)
  * // [com.example.Foo.bar(Foo.scala:42)] Hello
  *
  * SourceLocationFormat.afterMessage(" (", ")").render("Hello", location)
  * // Hello (com.example.Foo.bar(Foo.scala:42))
  * }}}
  *
  * @author Kevin Lee
  * @since 2026-11-22
  */
final case class SourceLocationFormat(
  position: SourceLocationFormat.Position,
  prefix: String,
  suffix: String,
)

object SourceLocationFormat {

  sealed trait Position
  object Position {
    case object BeforeMessage extends Position
    case object AfterMessage extends Position

    def beforeMessage: Position = BeforeMessage
    def afterMessage: Position  = AfterMessage
  }

  /** Renders `message at com.example.Foo.bar(Foo.scala:42)`. */
  val default: SourceLocationFormat = afterMessage(" at ", "")

  def beforeMessage(prefix: String, suffix: String): SourceLocationFormat =
    SourceLocationFormat(Position.BeforeMessage, prefix, suffix)

  def afterMessage(prefix: String, suffix: String): SourceLocationFormat =
    SourceLocationFormat(Position.AfterMessage, prefix, suffix)

  implicit class SourceLocationFormatOps(private val sourceLocationFormat: SourceLocationFormat) extends AnyVal {

    def render(message: String, sourceLocation: SourceLocation): String = sourceLocationFormat match {
      case SourceLocationFormat(Position.BeforeMessage, prefix, suffix) =>
        s"$prefix${sourceLocation.render}$suffix$message"

      case SourceLocationFormat(Position.AfterMessage, prefix, suffix) =>
        s"$message$prefix${sourceLocation.render}$suffix"
    }
  }
}
