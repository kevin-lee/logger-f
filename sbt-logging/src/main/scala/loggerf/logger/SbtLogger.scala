package loggerf.logger

final class SbtLogger(val log: sbt.util.Logger) extends Logger {

  override def debug(message: String): Unit = log.debug(message)

  override def info(message: String): Unit = log.info(message)

  override def warn(message: String): Unit = log.warn(message)

  override def error(message: String): Unit = log.error(message)
}

object SbtLogger {
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def sbtLogger(implicit log: sbt.util.Logger): Logger = new SbtLogger(log)
}
