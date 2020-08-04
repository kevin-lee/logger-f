package loggerf.logger

import scala.reflect.ClassTag

final class Slf4JLogger(val logger: org.slf4j.Logger) extends Logger {

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

  def slf4JLogger[A](implicit aClass: ClassTag[A]): Logger =
    new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(aClass.runtimeClass))

  def slf4JLogger(name: String): Logger =
    new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(name))

  def slf4JLoggerWith(logger: org.slf4j.Logger): Logger =
    new Slf4JLogger(logger)

}