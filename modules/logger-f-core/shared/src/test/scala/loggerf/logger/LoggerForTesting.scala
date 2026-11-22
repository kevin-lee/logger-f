package loggerf.logger

import loggerf.SourceLocation
import loggerf.logger.LoggerForTesting.MessageKeeper

/** @author Kevin Lee
  * @since 2020-04-12
  */
final case class LoggerForTesting private (
  logger: MessageKeeper
) extends CanLog {
  override def debug(message: => String): Unit =
    logger.debugMessages = logger.debugMessages :+ message

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  override def debug(throwable: Throwable)(message: => String): Unit =
    logger.debugMessages = logger.debugMessages :+ s"$message\n${throwable.toString}"

  override def info(message: => String): Unit =
    logger.infoMessages = logger.infoMessages :+ message

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  override def info(throwable: Throwable)(message: => String): Unit =
    logger.infoMessages = logger.infoMessages :+ s"$message\n${throwable.toString}"

  override def warn(message: => String): Unit =
    logger.warnMessages = logger.warnMessages :+ message

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  override def warn(throwable: Throwable)(message: => String): Unit =
    logger.warnMessages = logger.warnMessages :+ s"$message\n${throwable.toString}"

  override def error(message: => String): Unit =
    logger.errorMessages = logger.errorMessages :+ message

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  override def error(throwable: Throwable)(message: => String): Unit =
    logger.errorMessages = logger.errorMessages :+ s"$message\n${throwable.toString}"

  /* The source location is dropped so that the recorded strings stay identical to the messages given. */
  override def debug(sourceLocation: SourceLocation)(message: => String): Unit = debug(message)

  override def debug(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    debug(throwable)(message)

  override def info(sourceLocation: SourceLocation)(message: => String): Unit = info(message)

  override def info(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    info(throwable)(message)

  override def warn(sourceLocation: SourceLocation)(message: => String): Unit = warn(message)

  override def warn(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    warn(throwable)(message)

  override def error(sourceLocation: SourceLocation)(message: => String): Unit = error(message)

  override def error(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    error(throwable)(message)
}

object LoggerForTesting {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class MessageKeeper(
    private var _debugMessages: Vector[String], // scalafix:ok DisableSyntax.var
    private var _infoMessages: Vector[String], // scalafix:ok DisableSyntax.var
    private var _warnMessages: Vector[String], // scalafix:ok DisableSyntax.var
    private var _errorMessages: Vector[String], // scalafix:ok DisableSyntax.var
  ) {
    def debugMessages: Vector[String] = _debugMessages

    private[LoggerForTesting] def debugMessages_=(debugMessages: Vector[String]): Unit = _debugMessages = debugMessages

    def infoMessages: Vector[String] = _infoMessages

    private[LoggerForTesting] def infoMessages_=(infoMessages: Vector[String]): Unit = _infoMessages = infoMessages

    def warnMessages: Vector[String] = _warnMessages

    private[LoggerForTesting] def warnMessages_=(warnMessages: Vector[String]): Unit = _warnMessages = warnMessages

    def errorMessages: Vector[String] = _errorMessages

    private[LoggerForTesting] def errorMessages_=(errorMessages: Vector[String]): Unit = _errorMessages = errorMessages
  }

  def apply(
    debugMessages: Vector[String],
    infoMessages: Vector[String],
    warnMessages: Vector[String],
    errorMessages: Vector[String],
  ): LoggerForTesting =
    LoggerForTesting(MessageKeeper(debugMessages, infoMessages, warnMessages, errorMessages))

  def apply(): LoggerForTesting =
    LoggerForTesting(
      MessageKeeper(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
    )
}
