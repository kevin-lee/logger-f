package loggerf.test_data

import hedgehog._
import loggerf.SourceLocation
import loggerf.logger.SourceLocationFormat

/** @author Kevin Lee
  * @since 2023-11-07
  */
object Gens {
  def genTestCases: Gen[TestCases] =
    for {
      id      <- Gen.int(Range.linear(1, Int.MaxValue))
      name    <- Gen.string(Gen.alphaNum, Range.linear(1, 10))
      enabled <- Gen.boolean
    } yield TestCases(id, name, enabled)

  def genThrowable: Gen[(Throwable, Throwable, Throwable, Throwable)] = for {
    debugThrowableMessage <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(msg => s"test DEBUG Throwable: $msg")
    debugThrowable = new RuntimeException(debugThrowableMessage)

    infoThrowableMessage <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(msg => s"test INFO Throwable: $msg")
    infoThrowable = new RuntimeException(infoThrowableMessage)

    warnThrowableMessage <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(msg => s"test WARN Throwable: $msg")
    warnThrowable = new RuntimeException(warnThrowableMessage)

    errorThrowableMessage <- Gen.string(Gen.unicode, Range.linear(1, 20)).map(msg => s"test ERROR Throwable: $msg")
    errorThrowable = new RuntimeException(errorThrowableMessage)
  } yield (debugThrowable, infoThrowable, warnThrowable, errorThrowable)

  def genSourceLocation: Gen[SourceLocation] = for {
    enclosingClass  <- Gen.string(Gen.alpha, Range.linear(1, 10)).map(name => s"com.example.$name")
    enclosingMethod <- Gen.string(Gen.alpha, Range.linear(1, 10))
    fileName        <- Gen.string(Gen.alpha, Range.linear(1, 10)).map(name => s"$name.scala")
    line            <- Gen.int(Range.linear(1, 10000))
  } yield SourceLocation(enclosingClass, enclosingMethod, fileName, line)

  def genSourceLocationFormatPosition: Gen[SourceLocationFormat.Position] =
    Gen.element1(SourceLocationFormat.Position.beforeMessage, SourceLocationFormat.Position.afterMessage)

  def genSourceLocationFormat: Gen[SourceLocationFormat] = for {
    position <- genSourceLocationFormatPosition
    prefix   <- Gen.string(Gen.unicode, Range.linear(0, 5))
    suffix   <- Gen.string(Gen.unicode, Range.linear(0, 5))
  } yield SourceLocationFormat(position, prefix, suffix)

  def genMessage: Gen[String] = Gen.string(Gen.unicode, Range.linear(1, 20))

}
