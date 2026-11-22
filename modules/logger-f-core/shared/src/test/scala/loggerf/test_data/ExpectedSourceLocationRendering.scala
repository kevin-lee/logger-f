package loggerf.test_data

import loggerf.SourceLocation
import loggerf.logger.SourceLocationFormat

/** Builds the message that a `CanLog` should log for a given `SourceLocationFormat`.
  *
  * It does not call `SourceLocationFormat.render`, so tests which use it check `render` instead of repeating it.
  *
  * @author Kevin Lee
  * @since 2026-11-22
  */
object ExpectedSourceLocationRendering {

  def render(sourceLocationFormat: SourceLocationFormat, message: String, sourceLocation: SourceLocation): String =
    sourceLocationFormat.position match {
      case SourceLocationFormat.Position.BeforeMessage =>
        s"${sourceLocationFormat.prefix}${sourceLocation.render}${sourceLocationFormat.suffix}$message"

      case SourceLocationFormat.Position.AfterMessage =>
        s"$message${sourceLocationFormat.prefix}${sourceLocation.render}${sourceLocationFormat.suffix}"
    }

}
