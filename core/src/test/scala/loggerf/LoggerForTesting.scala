package loggerf

/**
 * @author Kevin Lee
 * @since 2020-04-12
 */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final case class LoggerForTesting private (
  private var debugMessages: Vector[String]
, private var infoMessages: Vector[String]
, private var warnMessages: Vector[String]
, private var errorMessages: Vector[String]
) extends Logger {
  override def debug(message: String): Unit =
    debugMessages = debugMessages :+ message

  override def info(message: String): Unit =
    infoMessages = infoMessages :+ message

  override def warn(message: String): Unit =
    warnMessages = warnMessages :+ message

  override def error(message: String): Unit =
    errorMessages = errorMessages :+ message
}

object LoggerForTesting {
  def apply(): LoggerForTesting =
    LoggerForTesting(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
}