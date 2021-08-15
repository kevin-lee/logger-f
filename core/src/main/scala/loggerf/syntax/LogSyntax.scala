package loggerf.syntax

import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2020-08-01
  */
trait LogSyntax {

  import loggerf.Level

  def getLogger(canLog: CanLog, level: Level): (=> String) => Unit = level match {
    case Level.Debug => canLog.debug
    case Level.Info  => canLog.info
    case Level.Warn  => canLog.warn
    case Level.Error => canLog.error
  }

}
