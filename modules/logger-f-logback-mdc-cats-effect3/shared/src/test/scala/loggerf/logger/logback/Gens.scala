package loggerf.logger.logback

import hedgehog._
import loggerf.logger.logback.types.{KeyValuePair, KeyValuePairs, SomeContext}

import java.time.Instant
import java.util.UUID

/** @author Kevin Lee
  * @since 2023-07-03
  */
object Gens {
  def genKeyValuePair: Gen[KeyValuePair] =
    for {
      key   <- Gen.string(Gen.alpha, Range.linear(1, 10))
      value <- Gen.string(Gen.alpha, Range.linear(1, 10))
    } yield KeyValuePair(key, value)

  def genKeyValuePairs: Gen[KeyValuePairs] =
    genKeyValuePair.list(Range.linear(2, 10)).map { keyValuePairs =>
      KeyValuePairs(keyValuePairs.zipWithIndex.map {
        case (keyValuePair, index) =>
          val prefix = index.toString
          keyValuePair
            .copy(key = s"$prefix:${keyValuePair.key}")
            .copy(value = s"$prefix:${keyValuePair.value}")
      })
    }

  val genSomeContext: Gen[SomeContext] = {
    for {
      uuid  <- Gen.constant(UUID.randomUUID())
      idNum <- Gen.int(Range.linear(1, 9999))
      other <- Gen.string(Gen.unicode, Range.linear(1, 10))
    } yield SomeContext(uuid, idNum, Instant.now.minusSeconds(1000L), SomeContext.SomeValue(other))
  }

}
