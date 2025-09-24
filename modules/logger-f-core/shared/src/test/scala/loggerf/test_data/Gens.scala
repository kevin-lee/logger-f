package loggerf.test_data

import hedgehog._

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

}
