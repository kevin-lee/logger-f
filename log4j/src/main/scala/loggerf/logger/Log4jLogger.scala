package loggerf.logger

import scala.reflect.ClassTag

final class Log4jLogger(
  val logger: org.apache.logging.log4j.Logger
) extends Logger {

  override def debug(message: String): Unit = logger.debug(message)

  override def info(message: String): Unit = logger.info(message)

  override def warn(message: String): Unit = logger.warn(message)

  override def error(message: String): Unit = logger.error(message)
}

object Log4jLogger {

  def log4jLogger[A](implicit aClassTag: ClassTag[A]): Logger =
    new Log4jLogger(org.apache.logging.log4j.LogManager.getLogger(aClassTag.runtimeClass))

  def log4jLogger(name: String): Logger =
    new Log4jLogger(org.apache.logging.log4j.LogManager.getLogger(name))

  def log4jLoggerWith(logger: org.apache.logging.log4j.Logger): Logger =
    new Log4jLogger(logger)
}