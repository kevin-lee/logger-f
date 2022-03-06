package loggerf.logger

import loggerf.Level

/** @author Kevin Lee
  * @since 2020-03-28
  */
trait CanLog {
  def debug(message: => String): Unit
  def info(message: => String): Unit
  def warn(message: => String): Unit
  def error(message: => String): Unit
}

object CanLog {
  implicit class GetLogger(val canLog: CanLog) extends AnyVal {
    @inline def getLogger(level: Level): (=> String) => Unit = level match {
      case Level.Debug => canLog.debug
      case Level.Info => canLog.info
      case Level.Warn => canLog.warn
      case Level.Error => canLog.error
    }
  }
}
