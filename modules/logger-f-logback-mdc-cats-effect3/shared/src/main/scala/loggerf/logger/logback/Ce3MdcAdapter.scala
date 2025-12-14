package loggerf.logger.logback

import cats.effect.{IOLocal, SyncIO}
import cats.syntax.all._
import ch.qos.logback.classic.LoggerContext
import logback_scala_interop.JLoggerFMdcAdapter
import org.slf4j.LoggerFactory

import java.util.{Map => JMap, Set => JSet}
import scala.jdk.CollectionConverters._

/** @author Kevin Lee
  * @since 2023-07-07
  */
class Ce3MdcAdapter extends JLoggerFMdcAdapter {

  private[this] lazy val localContext: IOLocal[Map[String, String]] =
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

  private lazy val threadLocalContext: ThreadLocal[Map[String, String]] = localContext.unsafeThreadLocal()

  override def put(key: String, `val`: String): Unit = {
    val unsafeThreadLocal = threadLocalContext
    unsafeThreadLocal.set(unsafeThreadLocal.get + (key -> `val`))
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.StringPlusAny"))
  override def get(key: String): String =
    threadLocalContext.get.getOrElse(key, null) // scalafix:ok DisableSyntax.null

  override def remove(key: String): Unit = {
    val unsafeThreadLocal = threadLocalContext
    unsafeThreadLocal.set(unsafeThreadLocal.get - key)
  }

  override def clear(): Unit = threadLocalContext.set(Map.empty[String, String])

  override def getCopyOfContextMap: JMap[String, String] = getPropertyMap0

  override def setContextMap0(contextMap: JMap[String, String]): Unit =
    threadLocalContext.set(contextMap.asScala.toMap)

  private def getPropertyMap0: JMap[String, String] = threadLocalContext.get.asJava

  override def getPropertyMap: JMap[String, String] = getPropertyMap0

  override def getKeys: JSet[String] = threadLocalContext.get.keySet.asJava

}
object Ce3MdcAdapter extends Ce3MdcAdapterOps

trait Ce3MdcAdapterOps {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  protected def initialize0(ce3MdcAdapter: Ce3MdcAdapter): Ce3MdcAdapter = {
    org.slf4j.SetMdcAdapter(ce3MdcAdapter)
    ce3MdcAdapter
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf"))
  protected def getLoggerContext(): LoggerContext =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  def initialize(): Ce3MdcAdapter =
    initializeWithCe3MdcAdapterAndLoggerContext(new Ce3MdcAdapter, getLoggerContext())

  def initializeWithCe3MdcAdapter(ce3MdcAdapter: Ce3MdcAdapter): Ce3MdcAdapter =
    initializeWithCe3MdcAdapterAndLoggerContext(ce3MdcAdapter, getLoggerContext())

  def initializeWithLoggerContext(loggerContext: LoggerContext): Ce3MdcAdapter =
    initializeWithCe3MdcAdapterAndLoggerContext(new Ce3MdcAdapter, loggerContext)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def initializeWithCe3MdcAdapterAndLoggerContext(
    ce3MdcAdapter: Ce3MdcAdapter,
    loggerContext: LoggerContext,
  ): Ce3MdcAdapter = {
    val adapter = initialize0(ce3MdcAdapter)

    loggerContext.setMDCAdapter(adapter)
    if (loggerContext.getMDCAdapter == adapter) {
      println("[DEBUG][LoggerContext] It's set by setMDCAdapter.")
      adapter
    } else {
      println(
        "[DEBUG][LoggerContext] The old setMDCAdapter doesn't replace `mdcAdapter` if it has already been set, " +
          "so it will use reflection to set it in the `mdcAdapter` field."
      )
      val loggerContextClass = classOf[LoggerContext]
      val field              = loggerContextClass.getDeclaredField("mdcAdapter")
      field.setAccessible(true)
      field.set(loggerContext, adapter)
      field.setAccessible(false)
      adapter
    }
  }
}
