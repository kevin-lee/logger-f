package loggerf

/**
 * @author Kevin Lee
 * @since 2020-03-28
 */
trait Logger {
  def debug(message: String): Unit
  def info(message: String): Unit
  def warn(message: String): Unit
  def error(message: String): Unit
}
