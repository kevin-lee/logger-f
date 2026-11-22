package core_testing

import loggerf.SourceLocation
import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2025-08-07
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final case class CanLogForTesting private (private var _logs: Vector[String]) extends CanLog {

  def logs: List[String] = _logs.toList

  override def debug(message: => String): Unit =
    _logs = _logs :+ s"[DEBUG] $message"

  override def debug(throwable: Throwable)(message: => String): Unit =
    _logs = _logs :+ s"[DEBUG] $message\n${throwable.toString}"

  override def info(message: => String): Unit =
    _logs = _logs :+ s"[INFO] $message"

  override def info(throwable: Throwable)(message: => String): Unit =
    _logs = _logs :+ s"[INFO] $message\n${throwable.toString}"

  override def warn(message: => String): Unit =
    _logs = _logs :+ s"[WARN] $message"

  override def warn(throwable: Throwable)(message: => String): Unit =
    _logs = _logs :+ s"[WARN] $message\n${throwable.toString}"

  override def error(message: => String): Unit =
    _logs = _logs :+ s"[ERROR] $message"

  override def error(throwable: Throwable)(message: => String): Unit =
    _logs = _logs :+ s"[ERROR] $message\n${throwable.toString}"

  /* The source location is dropped so that the recorded strings stay identical to the messages given. */
  override def debug(sourceLocation: SourceLocation)(message: => String): Unit = debug(message)

  override def debug(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    debug(throwable)(message)

  override def info(sourceLocation: SourceLocation)(message: => String): Unit = info(message)

  override def info(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    info(throwable)(message)

  override def warn(sourceLocation: SourceLocation)(message: => String): Unit = warn(message)

  override def warn(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    warn(throwable)(message)

  override def error(sourceLocation: SourceLocation)(message: => String): Unit = error(message)

  override def error(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    error(throwable)(message)

}
object CanLogForTesting {
  def apply(): CanLogForTesting = new CanLogForTesting(Vector.empty)
}
