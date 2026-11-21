package loggerf.logger

import sbt.util.Logger

trait SbtLogger extends CanLog
object SbtLogger {

  private final case class DefaultSbtLogger(
    logger: Logger,
    override protected val sourceLocationPosition: CanLog.SourceLocationPosition,
    override protected val sourceLocationPrefix: String,
    override protected val sourceLocationSuffix: String,
  ) extends SbtLogger {

    override protected def newCanLogWithNewSourceLocationConfig(
      sourceLocationPosition: CanLog.SourceLocationPosition,
      sourceLocationPrefix: String,
      sourceLocationSuffix: String,
    ): CanLog =
      copy(
        logger = logger,
        sourceLocationPosition = sourceLocationPosition,
        sourceLocationPrefix = sourceLocationPrefix,
        sourceLocationSuffix = sourceLocationSuffix,
      )

    override def debug(message: => String): Unit = logger.debug(message)

    override def debug(throwable: Throwable)(message: => String): Unit = {
      logger.debug(message)
      logger.trace(throwable)
    }

    override def info(message: => String): Unit = logger.info(message)

    override def info(throwable: Throwable)(message: => String): Unit = {
      logger.info(message)
      logger.trace(throwable)
    }

    override def warn(message: => String): Unit = logger.warn(message)

    override def warn(throwable: Throwable)(message: => String): Unit = {
      logger.warn(message)
      logger.trace(throwable)
    }

    override def error(message: => String): Unit = logger.error(message)

    override def error(throwable: Throwable)(message: => String): Unit = {
      logger.error(message)
      logger.trace(throwable)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def sbtLoggerCanLog(implicit logger: Logger): CanLog =
    DefaultSbtLogger(logger, CanLog.SourceLocationPosition.AfterMessage, "", "")

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  @deprecated(message = "Use SbtLogger.sbtLoggerCanLog(sbt.util.Logger) instead", since = "1.2.0")
  def sbtLogger(implicit logger: Logger): CanLog = sbtLoggerCanLog(logger)
}
