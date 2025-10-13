package loggerf.logger.logback

import java.time.Instant
import java.util.UUID

/** @author Kevin Lee
  * @since 2023-07-03
  */
object types {
  final case class KeyValuePair(key: String, value: String)

  final case class KeyValuePairs(keyValuePairs: List[KeyValuePair])

  final case class SomeContext(uuid: UUID, idNum: Int, timestamp: Instant, someValue: SomeContext.SomeValue)
  object SomeContext {
    final case class SomeValue(value: String)

  }

}
