package loggerf.logger.logback

import ch.qos.logback.classic.LoggerContext
import logback_scala_interop.JLoggerFMdcAdapter
import monix.execution.misc.Local
import org.slf4j.{LoggerFactory, MDC}

import java.util.{Map => JMap, Set => JSet}
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2023-02-18
  */
class Monix3MdcAdapter extends JLoggerFMdcAdapter {

  private[this] val localContext: Local[Map[String, String]] =
    Local[Map[String, String]](Map.empty[String, String])

  override def put(key: String, `val`: String): Unit =
    localContext.update(localContext() + (key -> `val`))

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  override def get(key: String): String =
    localContext().getOrElse(key, null) // scalafix:ok DisableSyntax.null

  override def remove(key: String): Unit = localContext.update(localContext() - key)

  override def clear(): Unit = localContext.clear()

  override def getCopyOfContextMap: JMap[String, String] = getPropertyMap0

  override def setContextMap0(contextMap: JMap[String, String]): Unit =
    localContext.update(contextMap.asScala.toMap)

  private def getPropertyMap0: JMap[String, String] = localContext().asJava

  override def getPropertyMap: JMap[String, String] = getPropertyMap0

  override def getKeys: JSet[String] = localContext().keySet.asJava

}
object Monix3MdcAdapter extends Monix3MdcAdapterOps

trait Monix3MdcAdapterOps {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  protected def initialize0(monix3MdcAdapter: Monix3MdcAdapter): Monix3MdcAdapter = {
    val field = classOf[MDC].getDeclaredField("mdcAdapter")
    field.setAccessible(true)
    field.set(null, monix3MdcAdapter) // scalafix:ok DisableSyntax.null
    field.setAccessible(false)
    monix3MdcAdapter
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  protected def getLoggerContext(): LoggerContext =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext] // scalafix:ok DisableSyntax.asInstanceOf

  def initialize(): Monix3MdcAdapter =
    initializeWithMonix3MdcAdapterAndLoggerContext(new Monix3MdcAdapter, getLoggerContext())

  def initializeWithMonix3MdcAdapter(monix3MdcAdapter: Monix3MdcAdapter): Monix3MdcAdapter =
    initializeWithMonix3MdcAdapterAndLoggerContext(monix3MdcAdapter, getLoggerContext())

  def initializeWithLoggerContext(loggerContext: LoggerContext): Monix3MdcAdapter =
    initializeWithMonix3MdcAdapterAndLoggerContext(new Monix3MdcAdapter, loggerContext)

  def initializeWithMonix3MdcAdapterAndLoggerContext(
    monix3MdcAdapter: Monix3MdcAdapter,
    loggerContext: LoggerContext,
  ): Monix3MdcAdapter = {
    val adapter = initialize0(monix3MdcAdapter)
    try {
      val field = classOf[LoggerContext].getDeclaredField("mdcAdapter")
      field.setAccessible(true)
      field.set(loggerContext, adapter)
      field.setAccessible(false)
      adapter
    } catch {
      case NonFatal(_) => adapter
    }
  }

}
