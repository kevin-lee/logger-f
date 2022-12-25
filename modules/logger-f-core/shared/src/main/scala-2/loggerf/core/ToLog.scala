package loggerf.core

/** @author Kevin Lee
  * @since 2022-02-18
  */
trait ToLog[A] {
  def toLogMessage(a: A): String
}

object ToLog {
  def apply[A: ToLog]: ToLog[A] = implicitly[ToLog[A]]

  def by[A](f: A => String): ToLog[A] = f(_)

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def fromToString[A]: ToLog[A] = _.toString

  @inline implicit val stringToLog: ToLog[String] = identity
}
