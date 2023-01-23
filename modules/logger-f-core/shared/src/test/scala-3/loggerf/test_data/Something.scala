package loggerf.test_data

import loggerf.core.ToLog

final case class Something(message: String)
object Something {
  given somethingToLog: ToLog[Something] = something => s"Something(message=${something.message})"
}
