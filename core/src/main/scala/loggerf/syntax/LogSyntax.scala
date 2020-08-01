package loggerf.syntax

import loggerf.logger.Logger

/**
 * @author Kevin Lee
 * @since 2020-08-01
 */
trait LogSyntax {

  import loggerf.Level

  def getLogger(logger: Logger, level: Level): String => Unit = level match {
    case Level.Debug => logger.debug
    case Level.Info => logger.info
    case Level.Warn => logger.warn
    case Level.Error => logger.error
  }

}
