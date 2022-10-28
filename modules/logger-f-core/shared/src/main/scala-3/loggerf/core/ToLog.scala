package loggerf.core

/** @author Kevin Lee
  * @since 2022-02-18
  */
trait ToLog[A] {
  def toLogMessage(a: A): String
}

object ToLog {
  def apply[A: ToLog]: ToLog[A] = summon[ToLog[A]]

  def by[A](f: A => String): ToLog[A] = f(_)

  given stringToLog: ToLog[String] = identity(_)
}
