package core_testing

import effectie.core._
import effectie.instances.tries.fx._
import hedgehog._
import hedgehog.runner._
import loggerf.core._
import loggerf.core.syntax.all._

import scala.util.{Success, Try}

/** @author Kevin Lee
  * @since 2025-08-05
  */
object LogWithCatsAndCustomLogInstanceSpec extends Properties {

  override def tests: List[Test] = List(
    property("test Log[Try] with a custom Log[Try] instance", testCustomInstance)
  )

  def f(n: Int): Int = n * 2

  def foo[F[*]: Fx: Log](n: Int): F[Int] = Log[F].log(Fx[F].effectOf(f(n)))(n => info(n.toString))

  def testCustomInstance: Property = for {
    n <- Gen.int(Range.linear(0, Int.MaxValue)).log("n")
  } yield {
    implicit val canLog: CanLogForTesting = CanLogForTesting()

    implicit val log: Log[Try] = TestLogUtils.logTry

    val actual   = foo[Try](n)
    val expected = Success(f(n))

    val actualLogs   = canLog.logs
    val expectedLogs = List(s"[INFO] ${f(n).toString}")

    Result.all(
      List(
        actual ==== expected,
        (actualLogs ==== expectedLogs).log("logged message doesn't match."),
      )
    )

  }
}
