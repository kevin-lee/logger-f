package loggerf.instances

import _root_.cats.Show
import loggerf.core.ToLog

/** @author Kevin Lee
  * @since 2022-02-19
  */
trait show {
  given showToLog[A: Show]: ToLog[A] with {
    inline def toLogMessage(a: A): String = Show[A].show(a)
  }
}

object show extends show
