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
    example("withoutSourceLocation drops the location with Throwable", testWithoutSourceLocationWithThrowable),
    example("getLoggerWithSourceLocation routes to the right level", testGetLoggerWithSourceLocation),
    example(
      "getLoggerWithThrowableAndSourceLocation routes to the right level",
      testGetLoggerWithThrowableAndSourceLocation,
    ),
  )

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.ToString"))
  final class PlainRecorder extends CanLog {
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

}
