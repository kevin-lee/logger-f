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
}
