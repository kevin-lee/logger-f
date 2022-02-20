package loggerf.core

/** @author Kevin Lee
  * @since 2022-02-18
  */
trait ToLog[A] {
  def toLogMessage(a: A): String
}

object ToLog {
  def apply[A: ToLog]: ToLog[A] = summon[ToLog[A]]
}