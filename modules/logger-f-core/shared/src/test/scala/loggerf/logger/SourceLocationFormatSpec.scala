package loggerf.logger

import hedgehog._
import hedgehog.runner._
import loggerf.SourceLocation

/** @author Kevin Lee
  * @since 2026-11-22
  */
object SourceLocationFormatSpec extends Properties {
  override def tests: List[Test] = List(
    example("default renders `message at location`", testDefault),
    property("beforeMessage renders prefix + location + suffix + message", testBeforeMessage),
    property("afterMessage renders message + prefix + location + suffix", testAfterMessage),
    property("empty prefix and suffix add no separator (BeforeMessage)", testBeforeMessageEmpty),
    property("empty prefix and suffix add no separator (AfterMessage)", testAfterMessageEmpty),
  )

  private val genSourceLocation: Gen[SourceLocation] = for {
    enclosingClass  <- Gen.string(Gen.alpha, Range.linear(1, 10))
    enclosingMethod <- Gen.string(Gen.alpha, Range.linear(1, 10))
    fileName        <- Gen.string(Gen.alpha, Range.linear(1, 10)).map(_ + ".scala")
    line            <- Gen.int(Range.linear(1, 10000))
  } yield SourceLocation(enclosingClass, enclosingMethod, fileName, line)

  private val genText: Gen[String] = Gen.string(Gen.unicode, Range.linear(0, 10))

  def testDefault: Result = {
    val loc = SourceLocation("com.example.Foo", "bar", "Foo.scala", 42)
    SourceLocationFormat.default.render("Hello", loc) ==== "Hello at com.example.Foo.bar(Foo.scala:42)"
  }

  def testBeforeMessage: Property = for {
    loc     <- genSourceLocation.log("loc")
    message <- genText.log("message")
    prefix  <- genText.log("prefix")
    suffix  <- genText.log("suffix")
  } yield {
    val expected = prefix + loc.render + suffix + message
    val actual   = SourceLocationFormat.beforeMessage(prefix, suffix).render(message, loc)
    actual ==== expected
  }

  def testAfterMessage: Property = for {
    loc     <- genSourceLocation.log("loc")
    message <- genText.log("message")
    prefix  <- genText.log("prefix")
    suffix  <- genText.log("suffix")
  } yield {
    val expected = message + prefix + loc.render + suffix
    val actual   = SourceLocationFormat.afterMessage(prefix, suffix).render(message, loc)
    actual ==== expected
  }

  def testBeforeMessageEmpty: Property = for {
    loc     <- genSourceLocation.log("loc")
    message <- genText.log("message")
  } yield {
    val expected = loc.render + message
    val actual   = SourceLocationFormat.beforeMessage("", "").render(message, loc)
    actual ==== expected
  }

  def testAfterMessageEmpty: Property = for {
    loc     <- genSourceLocation.log("loc")
    message <- genText.log("message")
  } yield {
    val expected = message + loc.render
    val actual   = SourceLocationFormat.afterMessage("", "").render(message, loc)
    actual ==== expected
  }

}
