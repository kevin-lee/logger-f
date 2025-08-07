package core_testing

import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2025-08-07
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final case class CanLogForTesting private (private var _logs: Vector[String]) extends CanLog {

  def logs: List[String] = _logs.toList

  override def debug(message: => String): Unit = _logs = _logs :+ s"[DEBUG] $message"

  override def info(message: => String): Unit = _logs = _logs :+ s"[INFO] $message"

  override def warn(message: => String): Unit = _logs = _logs :+ s"[WARN] $message"

  override def error(message: => String): Unit = _logs = _logs :+ s"[ERROR] $message"
}
object CanLogForTesting {
  def apply(): CanLogForTesting = new CanLogForTesting(Vector.empty)
}
