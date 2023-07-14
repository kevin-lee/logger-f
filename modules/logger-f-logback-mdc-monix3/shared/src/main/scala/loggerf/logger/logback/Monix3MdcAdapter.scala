package loggerf.logger.logback

import logback_scala_interop.JLoggerFMdcAdapter
import monix.execution.misc.Local
import org.slf4j.MDC

import java.util.{Map => JMap, Set => JSet}
import scala.jdk.CollectionConverters._

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
object Monix3MdcAdapter {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def initialize(): Monix3MdcAdapter = {
    val field   = classOf[MDC].getDeclaredField("mdcAdapter")
    field.setAccessible(true)
    val adapter = new Monix3MdcAdapter
    field.set(null, adapter) // scalafix:ok DisableSyntax.null
    field.setAccessible(false)
    adapter
  }
}
