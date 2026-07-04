package loggerf.logger.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

/** Captures each event's message and MDC map eagerly in append, which logback calls
  * synchronously on the logging thread, so getMDCPropertyMap resolves against that
  * thread's (fiber or fallback) context (it can be lazily initialized otherwise).
  *
  * @author Kevin Lee
  * @since 2026-07-04
  */
final class MdcCapturingAppender extends AppenderBase[ILoggingEvent] {
  private val eventQueue = new ConcurrentLinkedQueue[(String, Map[String, String])]()

  def capturedEvents: List[(String, Map[String, String])] = eventQueue.asScala.toList

  override def append(eventObject: ILoggingEvent): Unit = {
    val _ = eventQueue.add((eventObject.getFormattedMessage, eventObject.getMDCPropertyMap.asScala.toMap))
  }
}
