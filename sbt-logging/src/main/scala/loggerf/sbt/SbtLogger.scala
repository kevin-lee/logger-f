package loggerf.sbt

import sbt.util.Logger

final class SbtLogger(val log: Logger) extends loggerf.Logger {

  override def debug(message: String): Unit = log.debug(message)

  override def info(message: String): Unit = log.info(message)

  override def warn(message: String): Unit = log.warn(message)

  override def error(message: String): Unit = log.error(message)
}

object SbtLogger {
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def sbtLogger(implicit log: Logger): loggerf.Logger = new SbtLogger(log)
}
