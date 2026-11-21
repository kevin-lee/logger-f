package loggerf.logger

import org.apache.logging.log4j.{LogManager, Logger}

import scala.reflect.ClassTag

trait Log4jLogger extends CanLog
object Log4jLogger {
  private final case class DefaultLog4jLogger(
    logger: Logger,
    override protected val sourceLocationPosition: CanLog.SourceLocationPosition,
    override protected val sourceLocationPrefix: String,
    override protected val sourceLocationSuffix: String,
  ) extends Log4jLogger {

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

    override def debug(message: => String): Unit =
      logger.debug(Log4jCompat.toStringSupplier(message))

    override def debug(throwable: Throwable)(message: => String): Unit =
      logger.debug(Log4jCompat.toStringSupplier(message), throwable)

    override def info(message: => String): Unit =
      logger.info(Log4jCompat.toStringSupplier(message))

    override def info(throwable: Throwable)(message: => String): Unit =
      logger.info(Log4jCompat.toStringSupplier(message), throwable)

    override def warn(message: => String): Unit =
      logger.warn(Log4jCompat.toStringSupplier(message))

    override def warn(throwable: Throwable)(message: => String): Unit =
      logger.warn(Log4jCompat.toStringSupplier(message), throwable)

    override def error(message: => String): Unit =
      logger.error(Log4jCompat.toStringSupplier(message))

    override def error(throwable: Throwable)(message: => String): Unit =
      logger.error(Log4jCompat.toStringSupplier(message), throwable)

  }

  def log4jCanLog[A](implicit aClassTag: ClassTag[A]): CanLog =
    DefaultLog4jLogger(LogManager.getLogger(aClassTag.runtimeClass), CanLog.SourceLocationPosition.AfterMessage, "", "")

  def log4jCanLog(name: String): CanLog =
    DefaultLog4jLogger(LogManager.getLogger(name), CanLog.SourceLocationPosition.AfterMessage, "", "")

  def log4jCanLogWith(logger: Logger): CanLog =
    DefaultLog4jLogger(logger, CanLog.SourceLocationPosition.AfterMessage, "", "")

  @deprecated(message = "Use Log4jLogger.log4jCanLog[A] instead", since = "1.2.0")
  def log4jLogger[A](implicit aClassTag: ClassTag[A]): CanLog =
    log4jCanLog[A]

  @deprecated(message = "Use Log4jLogger.log4jCanLog(String) instead", since = "1.2.0")
  def log4jLogger(name: String): CanLog =
    log4jCanLog(name)

  @deprecated(
    message = "Use Log4jLogger.log4jCanLogWith(org.apache.logging.log4j.Logger) instead",
    since = "1.2.0",
  )
  def log4jLoggerWith(logger: Logger): CanLog =
    log4jCanLogWith(logger)
}
