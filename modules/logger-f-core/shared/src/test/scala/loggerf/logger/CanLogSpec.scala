package loggerf.logger

import hedgehog._
import hedgehog.runner._
import loggerf.{Level, SourceLocation}
import loggerf.test_data.{ExpectedSourceLocationRendering, Gens}

import java.util.concurrent.atomic.AtomicInteger

/** Verifies the default rendering of the location-aware `CanLog` methods and the `withoutSourceLocation` opt-out.
  *
  * @author Kevin Lee
  * @since 2026-09-20
  */
object CanLogSpec extends Properties {
  override def tests: List[Test] = List(
    example("default location overloads append the rendered location", testDefaultRendering),
    example("default throwable-and-location overloads append the rendered location", testDefaultRenderingWithThrowable),
    example("withoutSourceLocation drops the location", testWithoutSourceLocation),
    example(
      "withoutSourceLocation without SourceLocation given should do the same as withoutSourceLocation with SourceLocation passed (source location info dropped)",
      testWithoutSourceLocationWithoutSourceLocationPassed,
    ),
    example("withoutSourceLocation drops the location with Throwable", testWithoutSourceLocationWithThrowable),
    example(
      "withoutSourceLocation without SourceLocation given should do the same as withoutSourceLocation with Throwable (source location info dropped)",
      testWithoutSourceLocationWithThrowableWithoutSourceLocationPassed,
    ),
    example("getLoggerWithSourceLocation routes to the right level", testGetLoggerWithSourceLocation),
    example(
      "getLoggerWithThrowableAndSourceLocation routes to the right level",
      testGetLoggerWithThrowableAndSourceLocation,
    ),
    example("a custom sourceLocationFormat puts the location before the message", testCustomFormatBefore),
    example("a custom sourceLocationFormat puts the location after the message", testCustomFormatAfter),
    example(
      "a custom sourceLocationFormat with empty prefix and suffix adds no separator",
      testCustomFormatEmptyPrefixAndSuffix,
    ),
    example("a custom sourceLocationFormat with Throwable", testCustomFormatWithThrowable),
    example(
      "a custom sourceLocationFormat does not change the methods without SourceLocation",
      testCustomFormatWithoutSourceLocationPassed,
    ),
    example("the default sourceLocationFormat is SourceLocationFormat.default", testDefaultSourceLocationFormat),
    example("withoutSourceLocation drops the location with a custom sourceLocationFormat", testWithoutWithCustomFormat),
    example(
      "withoutSourceLocation keeps the sourceLocationFormat of the underlying CanLog",
      testWithoutKeepsSourceLocationFormat,
    ),
    example("withoutSourceLocation twice does not nest", testWithoutTwiceDoesNotNest),
    example("getLogger routes every level", testGetLogger),
    example("getLoggerWithSourceLocation routes every level", testGetLoggerWithSourceLocationAllLevels),
    example(
      "getLoggerWithThrowableAndSourceLocation routes every level",
      testGetLoggerWithThrowableAndSourceLocationAllLevels,
    ),
    property(
      "the default location-aware methods render the location with any sourceLocationFormat",
      testDefaultMethodsRenderWithAnyFormat,
    ),
    property(
      "the default location-aware methods with Throwable render the location with any sourceLocationFormat",
      testDefaultMethodsWithThrowableRenderWithAnyFormat,
    ),
    example(
      "the default location-aware methods do not evaluate the message if the backend does not use it",
      testDefaultMethodsAreLazy,
    ),
    example(
      "the default location-aware methods evaluate the message once if the backend uses it",
      testDefaultMethodsEvaluateMessageOnce,
    ),
    example(
      "withoutSourceLocation does not evaluate the message if the backend does not use it",
      testWithoutSourceLocationIsLazy,
    ),
    example(
      "withoutSourceLocation calls the methods without SourceLocation even if the backend overrides the location-aware methods",
      testWithoutSourceLocationSkipsLocationAwareOverrides,
    ),
    example("withoutSourceLocation wraps the given CanLog", testWithoutSourceLocationWrapsGivenCanLog),
  )

  /** Ignores every message, so the by-name message is never evaluated. */
  final class IgnoringCanLog extends CanLog {
    override def debug(message: => String): Unit                       = ()
    override def debug(throwable: Throwable)(message: => String): Unit = ()
    override def info(message: => String): Unit                        = ()
    override def info(throwable: Throwable)(message: => String): Unit  = ()
    override def warn(message: => String): Unit                        = ()
    override def warn(throwable: Throwable)(message: => String): Unit  = ()
    override def error(message: => String): Unit                       = ()
    override def error(throwable: Throwable)(message: => String): Unit = ()
  }

  /** Overrides the location-aware methods, like a backend which passes the location structurally. */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final class LocationAwareRecorder extends CanLog {
    @volatile private var _records: Vector[(Level, String)] = Vector.empty // scalafix:ok DisableSyntax.var

    @volatile private var _sourceLocations: Vector[SourceLocation] = Vector.empty // scalafix:ok DisableSyntax.var

    def records: Vector[(Level, String)] = _records

    def sourceLocations: Vector[SourceLocation] = _sourceLocations

    private def record(level: Level, message: String): Unit = _records = _records :+ ((level, message))

    private def recordWithLocation(level: Level, sourceLocation: SourceLocation, message: String): Unit = {
      _sourceLocations = _sourceLocations :+ sourceLocation
      record(level, message)
    }

    override def debug(message: => String): Unit                       = record(Level.debug, message)
    override def debug(throwable: Throwable)(message: => String): Unit = record(Level.debug, message)
    override def info(message: => String): Unit                        = record(Level.info, message)
    override def info(throwable: Throwable)(message: => String): Unit  = record(Level.info, message)
    override def warn(message: => String): Unit                        = record(Level.warn, message)
    override def warn(throwable: Throwable)(message: => String): Unit  = record(Level.warn, message)
    override def error(message: => String): Unit                       = record(Level.error, message)
    override def error(throwable: Throwable)(message: => String): Unit = record(Level.error, message)

    override def debug(sourceLocation: SourceLocation)(message: => String): Unit                       =
      recordWithLocation(Level.debug, sourceLocation, message)
    override def debug(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      recordWithLocation(Level.debug, sourceLocation, message)
    override def info(sourceLocation: SourceLocation)(message: => String): Unit                        =
      recordWithLocation(Level.info, sourceLocation, message)
    override def info(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit  =
      recordWithLocation(Level.info, sourceLocation, message)
    override def warn(sourceLocation: SourceLocation)(message: => String): Unit                        =
      recordWithLocation(Level.warn, sourceLocation, message)
    override def warn(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit  =
      recordWithLocation(Level.warn, sourceLocation, message)
    override def error(sourceLocation: SourceLocation)(message: => String): Unit                       =
      recordWithLocation(Level.error, sourceLocation, message)
    override def error(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      recordWithLocation(Level.error, sourceLocation, message)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.ToString"))
  final class PlainRecorder(override val sourceLocationFormat: SourceLocationFormat) extends CanLog {
    def this() = this(SourceLocationFormat.default)

    @volatile private var _records: Vector[(Level, String)] = Vector.empty // scalafix:ok DisableSyntax.var

    def records: Vector[(Level, String)] = _records

    override def debug(message: => String): Unit = _records = _records :+ ((Level.debug, message))

    override def debug(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.debug, s"$message\n${throwable.toString}"))

    override def info(message: => String): Unit = _records = _records :+ ((Level.info, message))

    override def info(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.info, s"$message\n${throwable.toString}"))

    override def warn(message: => String): Unit = _records = _records :+ ((Level.warn, message))

    override def warn(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.warn, s"$message\n${throwable.toString}"))

    override def error(message: => String): Unit = _records = _records :+ ((Level.error, message))

    override def error(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.error, s"$message\n${throwable.toString}"))
  }

  private val loc: SourceLocation = SourceLocation("com.example.Foo", "bar", "Foo.scala", 42)

  def testDefaultRendering: Result = {
    val recorder = new PlainRecorder
    recorder.debug(loc)("d")
    recorder.info(loc)("i")
    recorder.warn(loc)("w")
    recorder.error(loc)("e")
    recorder.records ==== Vector(
      (Level.debug, "d at com.example.Foo.bar(Foo.scala:42)"),
      (Level.info, "i at com.example.Foo.bar(Foo.scala:42)"),
      (Level.warn, "w at com.example.Foo.bar(Foo.scala:42)"),
      (Level.error, "e at com.example.Foo.bar(Foo.scala:42)"),
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testDefaultRenderingWithThrowable: Result = {
    val recorder  = new PlainRecorder
    val throwable = new RuntimeException("t")
    recorder.debug(throwable, loc)("d")
    recorder.info(throwable, loc)("i")
    recorder.warn(throwable, loc)("w")
    recorder.error(throwable, loc)("e")
    recorder.records ==== Vector(
      (Level.debug, s"d at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.info, s"i at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.warn, s"w at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.error, s"e at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
    )
  }

  def testWithoutSourceLocation: Result = {
    val recorder = new PlainRecorder
    val canLog   = recorder.withoutSourceLocation
    canLog.debug(loc)("d")
    canLog.info(loc)("i")
    canLog.warn(loc)("w")
    canLog.error(loc)("e")
    recorder.records ==== Vector((Level.debug, "d"), (Level.info, "i"), (Level.warn, "w"), (Level.error, "e"))
  }

  def testWithoutSourceLocationWithoutSourceLocationPassed: Result = {
    val recorder = new PlainRecorder
    val canLog   = recorder.withoutSourceLocation
    canLog.debug("d")
    canLog.info("i")
    canLog.warn("w")
    canLog.error("e")
    recorder.records ==== Vector((Level.debug, "d"), (Level.info, "i"), (Level.warn, "w"), (Level.error, "e"))
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testWithoutSourceLocationWithThrowable: Result = {
    val recorder  = new PlainRecorder
    val canLog    = recorder.withoutSourceLocation
    val throwable = new RuntimeException("t")
    canLog.debug(throwable, loc)("d")
    canLog.info(throwable, loc)("i")
    canLog.warn(throwable, loc)("w")
    canLog.error(throwable, loc)("e")
    recorder.records ==== Vector(
      (Level.debug, s"d\n${throwable.toString}"),
      (Level.info, s"i\n${throwable.toString}"),
      (Level.warn, s"w\n${throwable.toString}"),
      (Level.error, s"e\n${throwable.toString}"),
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testWithoutSourceLocationWithThrowableWithoutSourceLocationPassed: Result = {
    val recorder  = new PlainRecorder
    val canLog    = recorder.withoutSourceLocation
    val throwable = new RuntimeException("t")
    canLog.debug(throwable)("d")
    canLog.info(throwable)("i")
    canLog.warn(throwable)("w")
    canLog.error(throwable)("e")
    recorder.records ==== Vector(
      (Level.debug, s"d\n${throwable.toString}"),
      (Level.info, s"i\n${throwable.toString}"),
      (Level.warn, s"w\n${throwable.toString}"),
      (Level.error, s"e\n${throwable.toString}"),
    )
  }

  def testGetLoggerWithSourceLocation: Result = {
    val recorder = new PlainRecorder
    recorder.getLoggerWithSourceLocation(Level.info)(loc)("i")
    recorder.getLoggerWithSourceLocation(Level.error)(loc)("e")
    recorder.records ==== Vector(
      (Level.info, "i at com.example.Foo.bar(Foo.scala:42)"),
      (Level.error, "e at com.example.Foo.bar(Foo.scala:42)"),
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testGetLoggerWithThrowableAndSourceLocation: Result = {
    val recorder  = new PlainRecorder
    val throwable = new RuntimeException("t")
    recorder.getLoggerWithThrowableAndSourceLocation(Level.warn)(throwable)(loc)("w")
    recorder.getLoggerWithThrowableAndSourceLocation(Level.error)(throwable)(loc)("e")
    recorder.records ==== Vector(
      (Level.warn, s"w at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.error, s"e at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
    )
  }

  def testCustomFormatBefore: Result = {
    val recorder = new PlainRecorder(SourceLocationFormat.beforeMessage("[", "] "))
    recorder.debug(loc)("d")
    recorder.info(loc)("i")
    recorder.warn(loc)("w")
    recorder.error(loc)("e")
    recorder.records ==== Vector(
      (Level.debug, "[com.example.Foo.bar(Foo.scala:42)] d"),
      (Level.info, "[com.example.Foo.bar(Foo.scala:42)] i"),
      (Level.warn, "[com.example.Foo.bar(Foo.scala:42)] w"),
      (Level.error, "[com.example.Foo.bar(Foo.scala:42)] e"),
    )
  }

  def testCustomFormatAfter: Result = {
    val recorder = new PlainRecorder(SourceLocationFormat.afterMessage(" (", ")"))
    recorder.debug(loc)("d")
    recorder.info(loc)("i")
    recorder.warn(loc)("w")
    recorder.error(loc)("e")
    recorder.records ==== Vector(
      (Level.debug, "d (com.example.Foo.bar(Foo.scala:42))"),
      (Level.info, "i (com.example.Foo.bar(Foo.scala:42))"),
      (Level.warn, "w (com.example.Foo.bar(Foo.scala:42))"),
      (Level.error, "e (com.example.Foo.bar(Foo.scala:42))"),
    )
  }

  def testCustomFormatEmptyPrefixAndSuffix: Result = {
    val before = new PlainRecorder(SourceLocationFormat.beforeMessage("", ""))
    val after  = new PlainRecorder(SourceLocationFormat.afterMessage("", ""))
    before.info(loc)("before")
    after.info(loc)("after")
    Result.all(
      List(
        before.records ==== Vector((Level.info, "com.example.Foo.bar(Foo.scala:42)before")),
        after.records ==== Vector((Level.info, "aftercom.example.Foo.bar(Foo.scala:42)")),
      )
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testCustomFormatWithThrowable: Result = {
    val recorder  = new PlainRecorder(SourceLocationFormat.beforeMessage("[", "] "))
    val throwable = new RuntimeException("t")
    recorder.debug(throwable, loc)("d")
    recorder.info(throwable, loc)("i")
    recorder.warn(throwable, loc)("w")
    recorder.error(throwable, loc)("e")
    recorder.records ==== Vector(
      (Level.debug, s"[com.example.Foo.bar(Foo.scala:42)] d\n${throwable.toString}"),
      (Level.info, s"[com.example.Foo.bar(Foo.scala:42)] i\n${throwable.toString}"),
      (Level.warn, s"[com.example.Foo.bar(Foo.scala:42)] w\n${throwable.toString}"),
      (Level.error, s"[com.example.Foo.bar(Foo.scala:42)] e\n${throwable.toString}"),
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testCustomFormatWithoutSourceLocationPassed: Result = {
    val recorder  = new PlainRecorder(SourceLocationFormat.beforeMessage("[", "] "))
    val throwable = new RuntimeException("t")
    recorder.debug("d")
    recorder.info("i")
    recorder.warn(throwable)("w")
    recorder.error(throwable)("e")
    recorder.records ==== Vector(
      (Level.debug, "d"),
      (Level.info, "i"),
      (Level.warn, s"w\n${throwable.toString}"),
      (Level.error, s"e\n${throwable.toString}"),
    )
  }

  def testDefaultSourceLocationFormat: Result = {
    val canLog: CanLog = new CanLog {
      override def debug(message: => String): Unit                       = ()
      override def debug(throwable: Throwable)(message: => String): Unit = ()
      override def info(message: => String): Unit                        = ()
      override def info(throwable: Throwable)(message: => String): Unit  = ()
      override def warn(message: => String): Unit                        = ()
      override def warn(throwable: Throwable)(message: => String): Unit  = ()
      override def error(message: => String): Unit                       = ()
      override def error(throwable: Throwable)(message: => String): Unit = ()
    }
    canLog.sourceLocationFormat ==== SourceLocationFormat.default
  }

  def testWithoutWithCustomFormat: Result = {
    val recorder = new PlainRecorder(SourceLocationFormat.beforeMessage("[", "] "))
    recorder.withoutSourceLocation.info(loc)("i")
    recorder.records ==== Vector((Level.info, "i"))
  }

  def testWithoutKeepsSourceLocationFormat: Result = {
    val sourceLocationFormat = SourceLocationFormat.beforeMessage("[", "] ")
    val recorder             = new PlainRecorder(sourceLocationFormat)
    recorder.withoutSourceLocation.sourceLocationFormat ==== sourceLocationFormat
  }

  def testWithoutTwiceDoesNotNest: Result = {
    val recorder = new PlainRecorder
    val canLog   = recorder.withoutSourceLocation.withoutSourceLocation
    canLog.info(loc)("i")
    Result.all(
      List(
        recorder.records ==== Vector((Level.info, "i")),
        Result.assert(canLog match {
          case without: CanLog.WithoutSourceLocation =>
            without.underlying match {
              case _: PlainRecorder => true
              case _ => false
            }
          case _ => false
        }),
      )
    )
  }

  def testGetLogger: Result = {
    val recorder = new PlainRecorder(SourceLocationFormat.beforeMessage("[", "] "))
    recorder.getLogger(Level.debug)("d")
    recorder.getLogger(Level.info)("i")
    recorder.getLogger(Level.warn)("w")
    recorder.getLogger(Level.error)("e")
    recorder.records ==== Vector((Level.debug, "d"), (Level.info, "i"), (Level.warn, "w"), (Level.error, "e"))
  }

  def testGetLoggerWithSourceLocationAllLevels: Result = {
    val recorder = new PlainRecorder
    recorder.getLoggerWithSourceLocation(Level.debug)(loc)("d")
    recorder.getLoggerWithSourceLocation(Level.info)(loc)("i")
    recorder.getLoggerWithSourceLocation(Level.warn)(loc)("w")
    recorder.getLoggerWithSourceLocation(Level.error)(loc)("e")
    recorder.records ==== Vector(
      (Level.debug, "d at com.example.Foo.bar(Foo.scala:42)"),
      (Level.info, "i at com.example.Foo.bar(Foo.scala:42)"),
      (Level.warn, "w at com.example.Foo.bar(Foo.scala:42)"),
      (Level.error, "e at com.example.Foo.bar(Foo.scala:42)"),
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testGetLoggerWithThrowableAndSourceLocationAllLevels: Result = {
    val recorder  = new PlainRecorder
    val throwable = new RuntimeException("t")
    recorder.getLoggerWithThrowableAndSourceLocation(Level.debug)(throwable)(loc)("d")
    recorder.getLoggerWithThrowableAndSourceLocation(Level.info)(throwable)(loc)("i")
    recorder.getLoggerWithThrowableAndSourceLocation(Level.warn)(throwable)(loc)("w")
    recorder.getLoggerWithThrowableAndSourceLocation(Level.error)(throwable)(loc)("e")
    recorder.records ==== Vector(
      (Level.debug, s"d at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.info, s"i at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.warn, s"w at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
      (Level.error, s"e at com.example.Foo.bar(Foo.scala:42)\n${throwable.toString}"),
    )
  }

  def testDefaultMethodsRenderWithAnyFormat: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    sourceLocation       <- Gens.genSourceLocation.log("sourceLocation")
    message              <- Gens.genMessage.log("message")
  } yield {
    val recorder = new PlainRecorder(sourceLocationFormat)
    recorder.debug(sourceLocation)(message)
    recorder.info(sourceLocation)(message)
    recorder.warn(sourceLocation)(message)
    recorder.error(sourceLocation)(message)

    val expected = ExpectedSourceLocationRendering.render(sourceLocationFormat, message, sourceLocation)
    recorder.records ==== Vector(
      (Level.debug, expected),
      (Level.info, expected),
      (Level.warn, expected),
      (Level.error, expected),
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def testDefaultMethodsWithThrowableRenderWithAnyFormat: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    sourceLocation       <- Gens.genSourceLocation.log("sourceLocation")
    message              <- Gens.genMessage.log("message")
  } yield {
    val recorder  = new PlainRecorder(sourceLocationFormat)
    val throwable = new RuntimeException("t")
    recorder.debug(throwable, sourceLocation)(message)
    recorder.info(throwable, sourceLocation)(message)
    recorder.warn(throwable, sourceLocation)(message)
    recorder.error(throwable, sourceLocation)(message)

    val expected =
      s"${ExpectedSourceLocationRendering.render(sourceLocationFormat, message, sourceLocation)}\n${throwable.toString}"
    recorder.records ==== Vector(
      (Level.debug, expected),
      (Level.info, expected),
      (Level.warn, expected),
      (Level.error, expected),
    )
  }

  /** Calls all 8 location-aware methods once. */
  private def callAllLocationAwareMethods(canLog: CanLog, message: () => String): Unit = {
    val throwable = new RuntimeException("t")
    canLog.debug(loc)(message())
    canLog.debug(throwable, loc)(message())
    canLog.info(loc)(message())
    canLog.info(throwable, loc)(message())
    canLog.warn(loc)(message())
    canLog.warn(throwable, loc)(message())
    canLog.error(loc)(message())
    canLog.error(throwable, loc)(message())
  }

  /** Calls all 8 methods without `SourceLocation` once. */
  private def callAllMethodsWithoutLocation(canLog: CanLog, message: () => String): Unit = {
    val throwable = new RuntimeException("t")
    canLog.debug(message())
    canLog.debug(throwable)(message())
    canLog.info(message())
    canLog.info(throwable)(message())
    canLog.warn(message())
    canLog.warn(throwable)(message())
    canLog.error(message())
    canLog.error(throwable)(message())
  }

  private def countingMessage(counter: AtomicInteger): () => String = { () =>
    counter.incrementAndGet()
    "message"
  }

  def testDefaultMethodsAreLazy: Result = {
    val counter = new AtomicInteger(0)
    callAllLocationAwareMethods(new IgnoringCanLog, countingMessage(counter))
    counter.get() ==== 0
  }

  def testDefaultMethodsEvaluateMessageOnce: Result = {
    val counter  = new AtomicInteger(0)
    val recorder = new PlainRecorder
    callAllLocationAwareMethods(recorder, countingMessage(counter))
    Result.all(
      List(
        counter.get() ==== 8,
        recorder.records.length ==== 8,
      )
    )
  }

  def testWithoutSourceLocationIsLazy: Result = {
    val counter = new AtomicInteger(0)
    val canLog  = new IgnoringCanLog().withoutSourceLocation
    callAllLocationAwareMethods(canLog, countingMessage(counter))
    callAllMethodsWithoutLocation(canLog, countingMessage(counter))
    counter.get() ==== 0
  }

  def testWithoutSourceLocationSkipsLocationAwareOverrides: Result = {
    val recorder = new LocationAwareRecorder
    val canLog   = recorder.withoutSourceLocation
    callAllLocationAwareMethods(canLog, () => "m")
    Result.all(
      List(
        recorder.sourceLocations ==== Vector.empty[SourceLocation],
        recorder.records ==== Vector(
          (Level.debug, "m"),
          (Level.debug, "m"),
          (Level.info, "m"),
          (Level.info, "m"),
          (Level.warn, "m"),
          (Level.warn, "m"),
          (Level.error, "m"),
          (Level.error, "m"),
        ),
      )
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testWithoutSourceLocationWrapsGivenCanLog: Result = {
    val recorder = new PlainRecorder
    recorder.withoutSourceLocation match {
      case without: CanLog.WithoutSourceLocation =>
        Result.assert(without.underlying eq recorder).log("underlying should be the given CanLog")
      case other =>
        Result.failure.log(s"Expected CanLog.WithoutSourceLocation but got ${other.getClass.getName}")
    }
  }

}
