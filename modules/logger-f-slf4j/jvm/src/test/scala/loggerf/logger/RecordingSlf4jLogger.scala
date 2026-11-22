package loggerf.logger

import org.slf4j.Marker
import org.slf4j.event.{Level => Slf4jLevel}
import org.slf4j.helpers.LegacyAbstractLogger

/** An slf4j `Logger` which keeps what it logs, so tests can check it.
  *
  * @param enabledLevels the levels this logger logs. It ignores all other levels.
  *
  * @author Kevin Lee
  * @since 2026-11-22
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class RecordingSlf4jLogger(enabledLevels: Set[Slf4jLevel]) extends LegacyAbstractLogger {

  @volatile private var _records: Vector[RecordingSlf4jLogger.Record] = Vector.empty // scalafix:ok DisableSyntax.var

  def records: Vector[RecordingSlf4jLogger.Record] = _records

  override def getName(): String = "RecordingSlf4jLogger"

  override def isTraceEnabled(): Boolean = enabledLevels.contains(Slf4jLevel.TRACE)
  override def isDebugEnabled(): Boolean = enabledLevels.contains(Slf4jLevel.DEBUG)
  override def isInfoEnabled(): Boolean  = enabledLevels.contains(Slf4jLevel.INFO)
  override def isWarnEnabled(): Boolean  = enabledLevels.contains(Slf4jLevel.WARN)
  override def isErrorEnabled(): Boolean = enabledLevels.contains(Slf4jLevel.ERROR)

  override protected def getFullyQualifiedCallerName(): String = classOf[RecordingSlf4jLogger].getName

  override protected def handleNormalizedLoggingCall(
    level: Slf4jLevel,
    marker: Marker,
    messagePattern: String,
    arguments: Array[AnyRef],
    throwable: Throwable,
  ): Unit =
    _records = _records :+ RecordingSlf4jLogger.Record(level, messagePattern, Option(throwable))
}

object RecordingSlf4jLogger {

  final case class Record(level: Slf4jLevel, message: String, throwable: Option[Throwable])

  def allEnabled(): RecordingSlf4jLogger = new RecordingSlf4jLogger(Slf4jLevel.values().toSet)

  def allDisabled(): RecordingSlf4jLogger = new RecordingSlf4jLogger(Set.empty)

}
