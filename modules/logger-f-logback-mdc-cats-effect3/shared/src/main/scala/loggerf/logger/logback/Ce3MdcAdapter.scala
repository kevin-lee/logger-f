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

  private[this] val localContext: IOLocal[Map[String, String]] =
    unsafeCreateIOLocal(Map.empty[String, String], "local context")

  /* Not lazy so that constructing the adapter fails fast with CE's UnsupportedOperationException
   * when the JVM was started without -Dcats.effect.trackFiberContext=true,
   * instead of failing at the first MDC use deep inside logging. */
  private val threadLocalContext: ThreadLocal[Map[String, String]] = localContext.unsafeThreadLocal()

  /* Probe used to detect whether a fiber is currently running on this thread:
   * IOLocal#unsafeThreadLocal's set is a silent no-op without a fiber, so a write
   * that can be read back means a fiber is present. This indirection is needed
   * because IOFiber.currentIOFiber() is private to cats-effect. */
  private[this] val probeLocal: IOLocal[Boolean]     = unsafeCreateIOLocal(false, "fiber probe")
  private val probeThreadLocal: ThreadLocal[Boolean] = probeLocal.unsafeThreadLocal()

  /* Classic per-thread MDC storage used only when no fiber is running on the current
   * thread, so that fiber-unaware code (servlet filters, event-loop callbacks, etc.)
   * keeps the stock LogbackMDCAdapter behaviour instead of having its writes dropped.
   * Never merged with the fiber context: a thread can hold fallback values and also run
   * fiber segments (e.g. via evalOn), and merging would leak values across the two. */
  private val fallbackContext: ThreadLocal[Map[String, String]] =
    ThreadLocal.withInitial(() => Map.empty[String, String])

  private def unsafeCreateIOLocal[A](default: A, name: String): IOLocal[A] =
    IOLocal[A](default)
      .syncStep(100)
      .flatMap(
        _.leftMap(_ =>
          new Error(
            s"Failed to initialize the $name of the Ce3MdcAdapter."
          )
        ).liftTo[SyncIO]
      )
      .unsafeRunSync()

  private def inFiber: Boolean = {
    probeThreadLocal.set(true)
    val present = probeThreadLocal.get()
    if (present) probeThreadLocal.remove()
    present
  }

  private def currentContext(): ThreadLocal[Map[String, String]] =
    if (inFiber) threadLocalContext else fallbackContext

  override def put(key: String, `val`: String): Unit = {
    val context = currentContext()
    context.set(context.get + (key -> `val`))
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.StringPlusAny"))
  override def get(key: String): String =
    currentContext().get.getOrElse(key, null) // scalafix:ok DisableSyntax.null

  override def remove(key: String): Unit = {
    val context = currentContext()
    context.set(context.get - key)
  }

  override def clear(): Unit =
    if (inFiber) threadLocalContext.set(Map.empty[String, String])
    else fallbackContext.remove()

  override def getCopyOfContextMap: JMap[String, String] = getPropertyMap0

  override def setContextMap0(contextMap: JMap[String, String]): Unit =
    currentContext().set(contextMap.asScala.toMap)

  private def getPropertyMap0: JMap[String, String] = currentContext().get.asJava

  override def getPropertyMap: JMap[String, String] = getPropertyMap0

  override def getKeys: JSet[String] = currentContext().get.keySet.asJava

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
      adapter
    } else {
      /* The old LoggerContext#setMDCAdapter doesn't replace `mdcAdapter` if it has already been set,
       * so use reflection to set the `mdcAdapter` field. */
      val loggerContextClass = classOf[LoggerContext]
      val field              = loggerContextClass.getDeclaredField("mdcAdapter")
      field.setAccessible(true)
      field.set(loggerContext, adapter)
      field.setAccessible(false)
      adapter
    }
  }
}
