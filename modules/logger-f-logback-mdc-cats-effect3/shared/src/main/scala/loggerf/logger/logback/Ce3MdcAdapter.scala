package loggerf.logger.logback

import cats.effect.{IOLocal, SyncIO}
import cats.syntax.all._
import ch.qos.logback.classic.LoggerContext
import logback_scala_interop.JLoggerFMdcAdapter
import org.slf4j.{LoggerFactory, MDC}

import java.util.{Map => JMap, Set => JSet}
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2023-07-07
  */
class Ce3MdcAdapter extends JLoggerFMdcAdapter {

  private[this] val localContext: IOLocal[Map[String, String]] =
    IOLocal[Map[String, String]](Map.empty[String, String])
      .syncStep(100)
      .flatMap(
        _.leftMap(_ =>
          new Error(
            "Failed to initialize the local context of the Ce3MdcAdapter."
          )
        ).liftTo[SyncIO]
      )
      .unsafeRunSync()

  override def put(key: String, `val`: String): Unit = {
    val unsafeThreadLocal = localContext.unsafeThreadLocal()
    unsafeThreadLocal.set(unsafeThreadLocal.get + (key -> `val`))
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.StringPlusAny"))
  override def get(key: String): String =
    localContext.unsafeThreadLocal().get.getOrElse(key, null) // scalafix:ok DisableSyntax.null

  override def remove(key: String): Unit = {
    val unsafeThreadLocal = localContext.unsafeThreadLocal()
    unsafeThreadLocal.set(unsafeThreadLocal.get - key)
  }

  override def clear(): Unit = localContext.unsafeThreadLocal().set(Map.empty[String, String])

  override def getCopyOfContextMap: JMap[String, String] = getPropertyMap0

  override def setContextMap0(contextMap: JMap[String, String]): Unit =
    localContext.unsafeThreadLocal().set(contextMap.asScala.toMap)

  private def getPropertyMap0: JMap[String, String] = localContext.unsafeThreadLocal().get.asJava

  override def getPropertyMap: JMap[String, String] = getPropertyMap0

  override def getKeys: JSet[String] = localContext.unsafeThreadLocal().get.keySet.asJava

}
object Ce3MdcAdapter {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  private def initialize0(): Ce3MdcAdapter = {
    val field   = classOf[MDC].getDeclaredField("mdcAdapter")
    field.setAccessible(true)
    val adapter = new Ce3MdcAdapter
    field.set(null, adapter) // scalafix:ok DisableSyntax.null
    field.setAccessible(false)
    adapter
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf"))
  def initialize(): Ce3MdcAdapter = {
    val loggerContext =
      LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    initializeWithLoggerContext(loggerContext)
  }

  def initializeWithLoggerContext(loggerContext: LoggerContext): Ce3MdcAdapter = {
    val adapter = initialize0()
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
