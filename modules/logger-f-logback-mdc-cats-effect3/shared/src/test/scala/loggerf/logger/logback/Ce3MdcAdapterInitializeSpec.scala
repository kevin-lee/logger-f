package loggerf.logger.logback

import cats.effect._
import cats.effect.unsafe.IORuntime
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.LogbackMDCAdapter
import hedgehog._
import hedgehog.runner._
import org.slf4j.spi.MDCAdapter
import org.slf4j.{LoggerFactory, MDC}

/** Verifies the initializer variants of Ce3MdcAdapterOps: each must install the adapter
  * both into the slf4j MDC facade and onto the target LoggerContext, including the
  * reflection force-install path for logback versions whose LoggerContext#setMDCAdapter
  * silently keeps an already-set adapter.
  *
  * Neither Ce3MdcAdapter nor LogbackMDCAdapter overrides equals, so every ==== on
  * adapters below is a reference-identity check, which is exactly what installation
  * means here.
  *
  * No before()/MDC.clear() hygiene is needed in this suite: every test installs a
  * freshly constructed adapter, whose IOLocal and fallback maps start empty, and the
  * behaviour checks run in fresh root fibers.
  *
  * @author Kevin Lee
  * @since 2026-07-05
  */
object Ce3MdcAdapterInitializeSpec extends Properties {

  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global

  override def tests: List[Test] = List(
    property(
      "Ce3MdcAdapter.initializeWithCe3MdcAdapter should install the given adapter on the global LoggerContext and make MDC use it",
      testInitializeWithCe3MdcAdapter,
    ),
    property(
      "Ce3MdcAdapter.initializeWithLoggerContext should install a new adapter on the given LoggerContext and make MDC use it",
      testInitializeWithLoggerContext,
    ),
    example(
      "Ce3MdcAdapter.initializeWithCe3MdcAdapterAndLoggerContext should force-install via reflection when the LoggerContext refuses to replace its adapter",
      testForceInstallOnStubbornLoggerContext,
    ),
  )

  def testInitializeWithCe3MdcAdapter: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      val ce3MdcAdapter = new Ce3MdcAdapter
      val returned      = Ce3MdcAdapter.initializeWithCe3MdcAdapter(ce3MdcAdapter)

      /* This variant targets the global LoggerContext (the one behind LoggerFactory),
       * so that is where the given instance must end up. */
      val installedOnGlobalContext =
        (getLoggerContext.getMDCAdapter ==== (ce3MdcAdapter: MDCAdapter))
          .log("the given adapter was not installed on the global LoggerContext")

      val got = (for {
        _   <- IO(MDC.put(keyValuePair.key, keyValuePair.value))
        got <- IO(MDC.get(keyValuePair.key))
      } yield got).unsafeRunSync()

      Result.all(
        List(
          (returned ==== ce3MdcAdapter).log("the returned adapter is not the given instance"),
          installedOnGlobalContext,
          (got ==== keyValuePair.value).log("MDC does not route through the newly installed adapter"),
        )
      )
    }

  def testInitializeWithLoggerContext: Property =
    for {
      keyValuePair <- Gens.genKeyValuePair.log("keyValuePair")
    } yield {
      /* A dedicated (unstarted) LoggerContext: this variant must install onto the given
       * context, not the global one, while still wiring the slf4j MDC facade. */
      val loggerContext = new LoggerContext
      val returned      = Ce3MdcAdapter.initializeWithLoggerContext(loggerContext)

      val installedOnGivenContext =
        (loggerContext.getMDCAdapter ==== (returned: MDCAdapter))
          .log("the created adapter was not installed on the given LoggerContext")

      val got = (for {
        _   <- IO(MDC.put(keyValuePair.key, keyValuePair.value))
        got <- IO(MDC.get(keyValuePair.key))
      } yield got).unsafeRunSync()

      Result.all(
        List(
          installedOnGivenContext,
          (got ==== keyValuePair.value).log("MDC does not route through the newly created adapter"),
        )
      )
    }

  def testForceInstallOnStubbornLoggerContext: Result = {
    val stubbornLoggerContext = new StubbornLoggerContext
    val preExistingAdapter    = new LogbackMDCAdapter
    stubbornLoggerContext.setMDCAdapter(preExistingAdapter)

    /* Self-validation: prove the stubborn context really refuses a second set, otherwise
     * this test would silently go through the happy path instead of the reflection one. */
    stubbornLoggerContext.setMDCAdapter(new LogbackMDCAdapter)
    val setupIsStubborn =
      (stubbornLoggerContext.getMDCAdapter ==== (preExistingAdapter: MDCAdapter))
        .log(
          "test setup: StubbornLoggerContext replaced the adapter on a second setMDCAdapter — " +
            "it must refuse to, or this test no longer exercises the reflection branch"
        )

    val ce3MdcAdapter = new Ce3MdcAdapter
    val returned      = Ce3MdcAdapter.initializeWithCe3MdcAdapterAndLoggerContext(ce3MdcAdapter, stubbornLoggerContext)

    Result.all(
      List(
        setupIsStubborn,
        (returned ==== ce3MdcAdapter).log("the returned adapter is not the given instance"),
        (stubbornLoggerContext.getMDCAdapter ==== (ce3MdcAdapter: MDCAdapter))
          .log("the mdcAdapter field was not force-set via reflection on the stubborn LoggerContext"),
      )
    )
  }

  /** Mimics the old LoggerContext#setMDCAdapter, which kept an already-set adapter
    * instead of replacing it (current logback replaces and only warns), so the
    * reflection fallback of initializeWithCe3MdcAdapterAndLoggerContext can be
    * exercised regardless of the logback version on the test classpath.
    */
  private final class StubbornLoggerContext extends LoggerContext {
    override def setMDCAdapter(anAdapter: MDCAdapter): Unit =
      if (Option(getMDCAdapter).isEmpty) super.setMDCAdapter(anAdapter)
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf"))
  private def getLoggerContext: LoggerContext =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

}
