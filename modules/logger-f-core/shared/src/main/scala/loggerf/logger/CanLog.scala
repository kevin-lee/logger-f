package loggerf.logger

import loggerf.Level

import scala.annotation.implicitNotFound

/** @author Kevin Lee
  * @since 2020-03-28
  */
@implicitNotFound(
  """
  Could not find an implicit CanLog.
  You can probably find it from the loggerf.logger package.
  ---
  If you use slf4j or logback, get logger-f-slf4j then,

    import loggerf.logger._

    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[MyAppType]
    // or
    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[this.type]

    // or
    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("my-logger-name")

    // or
    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLogWith(org.slf4j.LoggerFactory.getLogger(getClass))

  ---
  If you use log4s, get logger-f-log4s then,

    import loggerf.logger._
    implicit val canLog: CanLog = Log4sLogger.log4sCanLog[MyAppType]
    // or
    implicit val canLog: CanLog = Log4sLogger.log4sCanLog[this.type]

    // or
    implicit val canLog: CanLog = Log4sLogger.log4sCanLog("my-logger-name")

    // or
    implicit val canLog: CanLog = Log4sLogger.log4sCanLogWith(org.log4s.getLogger)
  ---

  ---
  If you use log4j, get logger-f-log4j then,

    import loggerf.logger._
    implicit val canLog: CanLog = Log4jLogger.log4jCanLog[MyAppType]
    // or
    implicit val canLog: CanLog = Log4jLogger.log4jCanLog[this.type]

    // or
    implicit val canLog: CanLog = Log4jLogger.log4jCanLog("my-logger-name")

    // or
    implicit val canLog: CanLog = Log4jLogger.log4jCanLogWith(org.apache.logging.log4j.LogManager.getLogger(getClass))
  ---
  """
)
trait CanLog {
  def debug(message: => String): Unit
  def debug(throwable: Throwable)(message: => String): Unit
  def info(message: => String): Unit
  def info(throwable: Throwable)(message: => String): Unit
  def warn(message: => String): Unit
  def warn(throwable: Throwable)(message: => String): Unit
  def error(message: => String): Unit
  def error(throwable: Throwable)(message: => String): Unit
}

object CanLog {
  implicit class GetLogger(private val canLog: CanLog) extends AnyVal {
    @inline def getLogger(level: Level): (=> String) => Unit = level match {
      case Level.Debug => canLog.debug
      case Level.Info => canLog.info
      case Level.Warn => canLog.warn
      case Level.Error => canLog.error
    }

    @inline def getLoggerWithThrowable(level: Level): (Throwable) => (=> String) => Unit = level match {
      case Level.Debug => canLog.debug
      case Level.Info => canLog.info
      case Level.Warn => canLog.warn
      case Level.Error => canLog.error
    }
  }
}
