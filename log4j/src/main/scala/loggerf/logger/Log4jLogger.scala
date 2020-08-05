package loggerf.logger

import org.apache.logging.log4j.{LogManager, Logger}

import scala.reflect.ClassTag

final class Log4jLogger(
  val logger: Logger
) extends CanLog {

  override def debug(message: => String): Unit =
    logger.debug(Log4jCompat.toStringSupplier(message))

  override def info(message: => String): Unit =
    logger.info(Log4jCompat.toStringSupplier(message))

  override def warn(message: => String): Unit =
    logger.warn(Log4jCompat.toStringSupplier(message))

  override def error(message: => String): Unit =
    logger.error(Log4jCompat.toStringSupplier(message))
}

object Log4jLogger {

  def log4jCanLog[A](implicit aClassTag: ClassTag[A]): CanLog =
    new Log4jLogger(LogManager.getLogger(aClassTag.runtimeClass))

  def log4jCanLog(name: String): CanLog =
    new Log4jLogger(LogManager.getLogger(name))

  def log4jCanLogWith(logger: Logger): CanLog =
    new Log4jLogger(logger)

  @deprecated(message = "Use Log4jLogger.log4jCanLog[A] instead", since = "1.2.0")
  def log4jLogger[A](implicit aClassTag: ClassTag[A]): CanLog =
    log4jCanLog[A]

  @deprecated(message = "Use Log4jLogger.log4jCanLog(String) instead", since = "1.2.0")
  def log4jLogger(name: String): CanLog =
    log4jCanLog(name)

  @deprecated(
    message = "Use Log4jLogger.log4jCanLogWith(org.apache.logging.log4j.Logger) instead",
    since = "1.2.0"
  )
  def log4jLoggerWith(logger: Logger): CanLog =
    log4jCanLogWith(logger)
}