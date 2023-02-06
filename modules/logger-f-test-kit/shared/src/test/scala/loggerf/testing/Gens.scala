package loggerf.testing

import hedgehog._
import loggerf.Level

/** @author Kevin Lee
  * @since 2023-02-05
  */
object Gens {
  def genLevelAndMessage: Gen[(Level, String)] =
    for {
      level   <- Gen.element1(Level.debug, Level.info, Level.warn, Level.error)
      message <- Gen.string(Gen.unicode, Range.linear(1, 20))
    } yield (level, message)
}
