package loggerf.logger

import org.apache.logging.log4j.message.{Message, ParameterizedMessageFactory}
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import org.apache.logging.log4j.{Level => Log4jLevel, Marker}

import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Properties

/** A log4j `Logger` which keeps what it logs, so tests can check it.
  *
  * It uses `SimpleLogger` to decide if a level is enabled, but it keeps each log event instead of printing it.
  *
  * @param level the lowest level this logger logs. `Log4jLevel.OFF` turns off every level.
  *
  * @author Kevin Lee
  * @since 2026-11-22
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class RecordingLog4jLogger(level: Log4jLevel)
    extends SimpleLogger(
      "RecordingLog4jLogger",
      level,
      false,
      false,
      false,
      false,
      "",
      ParameterizedMessageFactory.INSTANCE,
      new PropertiesUtil(new Properties()),
      new PrintStream(new ByteArrayOutputStream()),
    ) {

  @volatile private var _records: Vector[RecordingLog4jLogger.Record] = Vector.empty // scalafix:ok DisableSyntax.var

  def records: Vector[RecordingLog4jLogger.Record] = _records

  override def logMessage(
    fqcn: String,
    messageLevel: Log4jLevel,
    marker: Marker,
    message: Message,
    throwable: Throwable,
  ): Unit =
    _records = _records :+ RecordingLog4jLogger.Record(messageLevel, message.getFormattedMessage, Option(throwable))
}

object RecordingLog4jLogger {

  final case class Record(level: Log4jLevel, message: String, throwable: Option[Throwable])

  def allEnabled(): RecordingLog4jLogger = new RecordingLog4jLogger(Log4jLevel.ALL)

  def allDisabled(): RecordingLog4jLogger = new RecordingLog4jLogger(Log4jLevel.OFF)

}
