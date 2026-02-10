package loggerf.logger.logback

import cats.effect.{IOLocal, unsafe}
import ch.qos.logback.classic.LoggerContext
import logback_scala_interop.JLoggerFMdcAdapter
import org.slf4j.LoggerFactory

import java.util.{Map => JMap, Set => JSet}
import scala.jdk.CollectionConverters._

/** @author Kevin Lee
  * @since 2023-07-07
  */
class Ce3MdcAdapterWithIoRuntime(private val ioRuntime: unsafe.IORuntime) extends JLoggerFMdcAdapter {

  private[this] val localContext: IOLocal[Map[String, String]] =
    IOLocal[Map[String, String]](Map.empty[String, String])
      .unsafeRunSync()(ioRuntime)

  private lazy val threadLocalContext: ThreadLocal[Map[String, String]] = localContext.unsafeThreadLocal()

  override def put(key: String, `val`: String): Unit = {
    val unsafeThreadLocal = threadLocalContext
    unsafeThreadLocal.set(unsafeThreadLocal.get + (key -> `val`))
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
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
object Ce3MdcAdapterWithIoRuntime extends Ce3MdcAdapterWithIoRuntimeOps

trait Ce3MdcAdapterWithIoRuntimeOps {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  protected def initialize0(ce3MdcAdapter: Ce3MdcAdapterWithIoRuntime): Ce3MdcAdapterWithIoRuntime = {
    org.slf4j.SetMdcAdapter(ce3MdcAdapter)
    ce3MdcAdapter
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf"))
  protected def getLoggerContext(): LoggerContext =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  def initialize()(implicit ioRuntime: unsafe.IORuntime): Ce3MdcAdapterWithIoRuntime =
    initializeWithCe3MdcAdapterWithIoRuntimeAndLoggerContext(
      new Ce3MdcAdapterWithIoRuntime(ioRuntime),
      getLoggerContext(),
    )

  def initializeWithCe3MdcAdapterWithIoRuntime(ce3MdcAdapter: Ce3MdcAdapterWithIoRuntime): Ce3MdcAdapterWithIoRuntime =
    initializeWithCe3MdcAdapterWithIoRuntimeAndLoggerContext(ce3MdcAdapter, getLoggerContext())

  def initializeWithLoggerContext(loggerContext: LoggerContext)(
    implicit ioRuntime: unsafe.IORuntime
  ): Ce3MdcAdapterWithIoRuntime =
    initializeWithCe3MdcAdapterWithIoRuntimeAndLoggerContext(new Ce3MdcAdapterWithIoRuntime(ioRuntime), loggerContext)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def initializeWithCe3MdcAdapterWithIoRuntimeAndLoggerContext(
    ce3MdcAdapter: Ce3MdcAdapterWithIoRuntime,
    loggerContext: LoggerContext,
  ): Ce3MdcAdapterWithIoRuntime = {
    val adapter = initialize0(ce3MdcAdapter)

    loggerContext.setMDCAdapter(adapter)
    if (loggerContext.getMDCAdapter == adapter) {
      println("[LoggerContext] It's set by setMDCAdapter.")
      adapter
    } else {
      println(
        "[LoggerContext] The old setMDCAdapter doesn't replace `mdcAdapter` if it has already been set, " +
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
