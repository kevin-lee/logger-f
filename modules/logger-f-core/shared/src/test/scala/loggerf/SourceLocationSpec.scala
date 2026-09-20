package loggerf

import hedgehog._
import hedgehog.runner._

/** @author Kevin Lee
  * @since 2026-09-20
  */
object SourceLocationSpec extends Properties {

  // The next line must stay on line 12. testPinned asserts its literal line number.
  val pinned: SourceLocation = implicitly[SourceLocation]

  override def tests: List[Test] = List(
    example("SourceLocation captured in an object body", testPinned),
    example("SourceLocation on consecutive lines", testConsecutiveLines),
    example("SourceLocation captured inside a method", testEnclosingMethod),
    example("SourceLocation captured inside a lambda", testInsideLambda),
    example("SourceLocation captured inside a nested object", testNestedObject),
    example("SourceLocation.render", testRender),
  )

  def testPinned: Result =
    Result.all(
      List(
        pinned.line ==== 12,
        pinned.fileName ==== "SourceLocationSpec.scala",
        pinned.enclosingClass ==== "loggerf.SourceLocationSpec",
        pinned.enclosingMethod ==== "<init>",
      )
    )

  def testConsecutiveLines: Result = {
    // format: off
    val first: SourceLocation  = implicitly[SourceLocation]
    val second: SourceLocation = implicitly[SourceLocation]
    // format: on
    second.line ==== first.line + 1
  }

  def testEnclosingMethod: Result = {
    val here: SourceLocation = implicitly[SourceLocation]
    Result.all(
      List(
        here.enclosingMethod ==== "testEnclosingMethod",
        here.enclosingClass ==== "loggerf.SourceLocationSpec",
        here.fileName ==== "SourceLocationSpec.scala",
      )
    )
  }

  def testInsideLambda: Result = {
    val locations: List[SourceLocation] = List(1).map(_ => implicitly[SourceLocation])
    locations.map(_.enclosingMethod) ==== List("testInsideLambda")
  }

  object Nested {
    def loc: SourceLocation = implicitly[SourceLocation]
  }

  def testNestedObject: Result =
    Result.all(
      List(
        Nested.loc.enclosingClass ==== "loggerf.SourceLocationSpec.Nested",
        Nested.loc.enclosingMethod ==== "loc",
      )
    )

  def testRender: Result =
    SourceLocation("a.B", "c", "B.scala", 7).render ==== "a.B.c(B.scala:7)"

}
