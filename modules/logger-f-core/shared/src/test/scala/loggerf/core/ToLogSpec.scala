package loggerf.core

import hedgehog._
import hedgehog.runner._

/** @author Kevin Lee
  * @since 2022-10-28
  */
object ToLogSpec extends Properties {
  override def tests: List[Test] = List(
    property("test ToLog.by[A](A => String)", testBy),
    property("test ToLog.fromToString[A]", testFromToString),
    property("test ToLog[String].toLogMessage", testStringToLog),
  )

  def testBy: Property =
    for {
      prefix <- Gen.string(Gen.unicode, Range.linear(1, 10)).log("prefix")
      s      <- Gen.string(Gen.unicode, Range.linear(5, 10)).log("s")
    } yield {
      val foo      = Foo(s)
      val fooToLog = ToLog.by[Foo](foo => prefix + foo.s)

      val expected = prefix + s
      val actual   = fooToLog.toLogMessage(foo)

      actual ==== expected
    }

  def testFromToString: Property =
    for {
      s <- Gen.string(Gen.unicode, Range.linear(5, 10)).log("s")
    } yield {
      val foo      = Foo(s)
      val fooToLog = ToLog.fromToString[Foo]

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val expected = foo.toString
      val actual   = fooToLog.toLogMessage(foo)

      actual ==== expected
    }

  def testStringToLog: Property =
    for {
      s <- Gen.string(Gen.unicode, Range.linear(5, 10)).log("s")
    } yield {
      val expected = s
      val actual   = ToLog[String].toLogMessage(s)
      actual ==== expected
    }

  final case class Foo(s: String)
}
