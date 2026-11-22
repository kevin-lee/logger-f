package loggerf.logger

import org.log4s.Logger

import scala.reflect.ClassTag

/** @author Kevin Lee
  * @since 2020-09-12
  */
final class Log4sLogger(val logger: Logger) extends CanLog {

  override def debug(message: => String): Unit = logger.debug(message)

  override def debug(throwable: Throwable)(message: => String): Unit = logger.debug(throwable)(message)

  override def info(message: => String): Unit = logger.info(message)

  override def info(throwable: Throwable)(message: => String): Unit = logger.info(throwable)(message)

  override def warn(message: => String): Unit = logger.warn(message)

  override def warn(throwable: Throwable)(message: => String): Unit = logger.warn(throwable)(message)

  override def error(message: => String): Unit = logger.error(message)

  override def error(throwable: Throwable)(message: => String): Unit = logger.error(throwable)(message)
}

object Log4sLogger {

  def log4sCanLog[A](implicit aClass: ClassTag[A]): CanLog =
    new Log4sLogger(org.log4s.getLogger(aClass.runtimeClass))

  def log4sCanLog(name: String): CanLog =
    new Log4sLogger(org.log4s.getLogger(name))

  def log4sCanLogWith(logger: Logger): CanLog =
    new Log4sLogger(logger)

}
