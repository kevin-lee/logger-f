package loggerf.logger

import hedgehog._
import hedgehog.runner._
import loggerf.{Level, SourceLocation}

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
  )

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

}
