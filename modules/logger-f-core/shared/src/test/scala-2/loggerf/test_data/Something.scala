package loggerf.test_data

import loggerf.core.ToLog

final case class Something(message: String)
object Something {
  implicit val somethingToLog: ToLog[Something] = something => s"Something(message=${something.message})"
}
