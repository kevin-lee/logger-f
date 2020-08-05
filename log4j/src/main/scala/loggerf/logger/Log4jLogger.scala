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

  def log4jLogger[A](implicit aClassTag: ClassTag[A]): CanLog =
    new Log4jLogger(LogManager.getLogger(aClassTag.runtimeClass))

  def log4jLogger(name: String): CanLog =
    new Log4jLogger(LogManager.getLogger(name))

  def log4jLoggerWith(logger: Logger): CanLog =
    new Log4jLogger(logger)
}