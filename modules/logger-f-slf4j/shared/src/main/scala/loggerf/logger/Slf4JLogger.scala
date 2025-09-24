package loggerf.logger

import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag

final class Slf4JLogger(val logger: Logger) extends CanLog {

  @inline private def constructLog(isAvailable: Boolean, logFunction: String => Unit): (=> String) => Unit =
    if (isAvailable)
      message => logFunction(message)
    else
      _ => ()

  @inline private def constructLogWithThrowable(
    isAvailable: Boolean,
    logFunction: (String, Throwable) => Unit,
  ): Throwable => (=> String) => Unit =
    if (isAvailable)
      throwable => message => logFunction(message, throwable)
    else
      _ => _ => ()

  @inline override def debug(message: => String): Unit =
    constructLog(logger.isDebugEnabled, logger.debug)(message)

  @inline override def debug(throwable: Throwable)(message: => String): Unit =
    constructLogWithThrowable(logger.isDebugEnabled, logger.debug)(throwable)(message)

  @inline override def info(message: => String): Unit =
    constructLog(logger.isInfoEnabled, logger.info)(message)

  @inline override def info(throwable: Throwable)(message: => String): Unit =
    constructLogWithThrowable(logger.isInfoEnabled, logger.info)(throwable)(message)

  @inline override def warn(message: => String): Unit =
    constructLog(logger.isWarnEnabled, logger.warn)(message)

  @inline override def warn(throwable: Throwable)(message: => String): Unit =
    constructLogWithThrowable(logger.isWarnEnabled, logger.warn)(throwable)(message)

  @inline override def error(message: => String): Unit =
    constructLog(logger.isErrorEnabled, logger.error)(message)

  @inline override def error(throwable: Throwable)(message: => String): Unit =
    constructLogWithThrowable(logger.isErrorEnabled, logger.error)(throwable)(message)
}

object Slf4JLogger {

  def slf4JCanLog[A](implicit aClass: ClassTag[A]): CanLog =
    new Slf4JLogger(LoggerFactory.getLogger(aClass.runtimeClass))

  def slf4JCanLog(name: String): CanLog =
    new Slf4JLogger(LoggerFactory.getLogger(name))

  def slf4JCanLogWith(logger: Logger): CanLog =
    new Slf4JLogger(logger)

  @deprecated(message = "Use Slf4JLogger.slf4JCanLog[A] instead", since = "1.2.0")
  def slf4JLogger[A](implicit aClass: ClassTag[A]): CanLog =
    slf4JCanLog[A]

  @deprecated(message = "Use Slf4JLogger.slf4JCanLog(String) instead", since = "1.2.0")
  def slf4JLogger(name: String): CanLog =
    slf4JCanLog(name)

  @deprecated(message = "Use Slf4JLogger.slf4JLoggerWith(org.slf4j.Logger) instead", since = "1.2.0")
  def slf4JLoggerWith(logger: Logger): CanLog =
    slf4JCanLogWith(logger)

}
