package loggerf.logger

import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag

final class Slf4JLogger(val logger: Logger) extends CanLog {

  private def constructLog(isAvailable: Boolean, logFunction: String => Unit): (=> String) => Unit =
    if (isAvailable)
      message => logFunction(message)
    else
      _ => ()

  val debug0: (=> String) => Unit =
    constructLog(logger.isDebugEnabled, logger.debug)

  override def debug(message: => String): Unit = debug0(message)

  val info0: (=> String) => Unit =
    constructLog(logger.isInfoEnabled, logger.info)

  override def info(message: => String): Unit = info0(message)

  val warn0: (=> String) => Unit =
    constructLog(logger.isWarnEnabled, logger.warn)

  override def warn(message: => String): Unit = warn0(message)

  val error0: (=> String) => Unit =
    constructLog(logger.isErrorEnabled, logger.error)

  override def error(message: => String): Unit = error0(message)

}

object Slf4JLogger {

  def slf4JCanLog[A](implicit aClass: ClassTag[A]): CanLog =
    new Slf4JLogger(LoggerFactory.getLogger(aClass.runtimeClass))

  def slf4JCanLog(name: String): CanLog =
    new Slf4JLogger(LoggerFactory.getLogger(name))

  def slf4JCanLogWith(logger: Logger): CanLog =
    new Slf4JLogger(logger)

  @deprecated(message = "Use Slf4JLogger.slf4JCanLog[A] instead", since ="1.2.0")
  def slf4JLogger[A](implicit aClass: ClassTag[A]): CanLog =
    slf4JCanLog[A]

  @deprecated(message = "Use Slf4JLogger.slf4JCanLog(String) instead", since = "1.2.0")
  def slf4JLogger(name: String): CanLog =
    slf4JCanLog(name)

  @deprecated(message = "Use Slf4JLogger.slf4JLoggerWith(org.slf4j.Logger) instead", since = "1.2.0")
  def slf4JLoggerWith(logger: Logger): CanLog =
    slf4JCanLogWith(logger)

}