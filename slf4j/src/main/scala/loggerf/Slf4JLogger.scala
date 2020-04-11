package loggerf

import scala.reflect.ClassTag

final class Slf4JLogger(logger: org.slf4j.Logger) extends Logger {

  override def debug(message: String): Unit = logger.debug(message)

  override def info(message: String): Unit = logger.info(message)

  override def warn(message: String): Unit = logger.warn(message)

  override def error(message: String): Unit = logger.error(message)
}

object Slf4JLogger {

  def slf4JLogger[A](implicit aClass: ClassTag[A]): Logger =
    new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(aClass.runtimeClass))

  def slf4JLogger(name: String): Logger =
    new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(name))

}