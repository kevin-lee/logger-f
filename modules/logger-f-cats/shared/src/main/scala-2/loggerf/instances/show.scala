package loggerf.instances

import _root_.cats.Show
import loggerf.core.ToLog

/** @author Kevin Lee
  * @since 2022-02-19
  */
trait show {
  @inline implicit def showToLog[A: Show]: ToLog[A] = Show[A].show(_)
}

object show extends show
