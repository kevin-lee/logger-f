package loggerf.logger

final class SbtLogger(val logger: sbt.util.Logger) extends Logger {

  override def debug(message: String): Unit = logger.debug(message)

  override def info(message: String): Unit = logger.info(message)

  override def warn(message: String): Unit = logger.warn(message)

  override def error(message: String): Unit = logger.error(message)
}

object SbtLogger {
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def sbtLogger(implicit log: sbt.util.Logger): Logger = new SbtLogger(log)
}
