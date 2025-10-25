package loggerf.testings

import loggerf.test_data.TestCases
import cats.syntax.all._

import scala.util.Random

object RandomGens {
  val AlphaChars: Seq[Char]    = ('a' to 'z') ++ ('A' to 'Z')
  val AlphaNumChars: Seq[Char] = AlphaChars ++ ('0' to '9')

  val UnicodeChars: Seq[(Int, Int)] = List(0 -> 55295, 57344 -> 65533)

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def genRandomIntWithMinMax(min: Int, max: Int): Int = {
    if (min > max) {
      throw new IllegalArgumentException(
        s"min (${min.toString}) must be less than or equal to max (${max.toString})"
      ) // scalafix:ok DisableSyntax.throw
    } else {
      val bound = max.toLong - min.toLong + 1L
      if (bound <= Int.MaxValue) {
        Random.nextInt(bound.toInt) + min
      } else {
        (Random.nextInt() & Int.MaxValue) + min
      }
    }
  }

  val int2IntFunctions: List[Int => Int] = List[Int => Int](
    identity[Int],
    x => x + x,
    x => x - x,
    x => x * x,
    x => x + 100,
    x => x - 100,
    x => x * 100,
  )

  @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
  def genRandomIntToInt(): Int => Int = {
    val length = int2IntFunctions.length
    val index  = RandomGens.genRandomIntWithMinMax(0, length - 1)
    int2IntFunctions(index)
  }

  def genRandomInt(): Int = genRandomIntWithMinMax(0, Int.MaxValue)

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def genRandomLongWithMinMax(min: Long, max: Long): Long = {
    if (min > max) {
      throw new IllegalArgumentException(
        s"min (${min.toString}) must be less than or equal to max (${max.toString})"
      ) // scalafix:ok DisableSyntax.throw
    } else {
      val bound = max - min + 1L
      if (bound <= Long.MaxValue) {
        Random.nextLong(bound) + min
      } else {
        (Random.nextLong() & Long.MaxValue) + min
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
  def genUnicodeString(min: Int, max: Int): String = {
    val length = Random.nextInt(max + 1 - min) + min
    (1 to length).map { _ =>
      val index        = Random.nextInt(1)
      val (start, end) = UnicodeChars(index)
      val charInt      = Random.nextInt(end + 1 - start) + start
      charInt.toChar
    }.mkString
  }

  @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
  def genAlphaString(length: Int): String =
    (1 to length).map(_ => AlphaChars(Random.nextInt(AlphaChars.length))).mkString

  def genAlphaStringList(length: Int, listSize: Int): List[String] =
    (1 to listSize).map(_ => genAlphaString(length)).toList

  @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
  def genAlphaNumericString(length: Int): String =
    (1 to length).map(_ => AlphaNumChars(Random.nextInt(AlphaNumChars.length))).mkString

  def genAlphaNumericStringList(length: Int, listSize: Int): List[String] =
    (1 to listSize).map(_ => genAlphaNumericString(length)).toList

  def genBoolean(): Boolean = genRandomIntWithMinMax(0, 1) === 0

  def genTestCases: TestCases = {
    val id      = genRandomIntWithMinMax(1, Int.MaxValue)
    val name    = genAlphaNumericString(10)
    val enabled = genBoolean()
    TestCases(id, name, enabled)
  }

  def genThrowable: (Throwable, Throwable, Throwable, Throwable) = {
    val debugThrowableMessage = s"test DEBUG Throwable: ${genAlphaNumericString(20)}"
    val debugThrowable        = new RuntimeException(debugThrowableMessage)

    val infoThrowableMessage = s"test INFO Throwable: ${genAlphaNumericString(20)}"
    val infoThrowable        = new RuntimeException(infoThrowableMessage)

    val warnThrowableMessage = s"test WARN Throwable: ${genAlphaNumericString(20)}"
    val warnThrowable        = new RuntimeException(warnThrowableMessage)

    val errorThrowableMessage = s"test ERROR Throwable: ${genAlphaNumericString(20)}"
    val errorThrowable        = new RuntimeException(errorThrowableMessage)

    (debugThrowable, infoThrowable, warnThrowable, errorThrowable)
  }

}
