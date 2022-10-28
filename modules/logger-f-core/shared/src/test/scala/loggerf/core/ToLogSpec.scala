package loggerf.core

import hedgehog._
import hedgehog.runner._

/** @author Kevin Lee
  * @since 2022-10-28
  */
object ToLogSpec extends Properties {
  override def tests: List[Test] = List(
    property("test ToLog.by", testBy)
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

  final case class Foo(s: String)
}
