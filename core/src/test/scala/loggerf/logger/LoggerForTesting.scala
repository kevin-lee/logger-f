package loggerf.logger

import loggerf.logger.LoggerForTesting.MessageKeeper

/**
 * @author Kevin Lee
 * @since 2020-04-12
 */
final case class LoggerForTesting private (
  logger: MessageKeeper
) extends Logger {
  override def debug(message: String): Unit =
    logger.debugMessages = logger.debugMessages :+ message

  override def info(message: String): Unit =
    logger.infoMessages = logger.infoMessages :+ message

  override def warn(message: String): Unit =
    logger.warnMessages = logger.warnMessages :+ message

  override def error(message: String): Unit =
    logger.errorMessages = logger.errorMessages :+ message
}

object LoggerForTesting {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class MessageKeeper(
    private var _debugMessages: Vector[String],
    private var _infoMessages: Vector[String],
    private var _warnMessages: Vector[String],
    private var _errorMessages: Vector[String]
  ) {
    def debugMessages: Vector[String] = _debugMessages
    private[LoggerForTesting] def debugMessages_=(debugMessages: Vector[String]): Unit =
      _debugMessages = debugMessages

    def infoMessages: Vector[String] = _infoMessages
    private[LoggerForTesting] def infoMessages_=(infoMessages: Vector[String]): Unit =
      _infoMessages = infoMessages

    def warnMessages: Vector[String] = _warnMessages
    private[LoggerForTesting] def warnMessages_=(warnMessages: Vector[String]): Unit =
      _warnMessages = warnMessages

    def errorMessages: Vector[String] = _errorMessages
    private[LoggerForTesting] def errorMessages_=(errorMessages: Vector[String]): Unit =
      _errorMessages = errorMessages
  }

  def apply(
    debugMessages: Vector[String],
    infoMessages: Vector[String],
    warnMessages: Vector[String],
    errorMessages: Vector[String]
  ): LoggerForTesting =
    LoggerForTesting(MessageKeeper(debugMessages, infoMessages, warnMessages, errorMessages))

  def apply(): LoggerForTesting =
    LoggerForTesting(
      MessageKeeper(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
    )
}