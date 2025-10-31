package loggerf.doobie1

import cats.effect._
import doobie.util.log.{ExecFailure, Parameters, ProcessingFailure, Success}
import effectie.instances.ce3.fx.ioFx
import extras.hedgehog.ce3.syntax.runner._
import hedgehog._
import hedgehog.runner._
import loggerf.testing.CanLog4Testing
import loggerf.testing.CanLog4Testing.OrderedMessages

import scala.concurrent.duration._

/** @author Kevin Lee
  * @since 2023-07-29
  */
object LoggerFLogHandlerSpec extends Properties {

  type F[A] = IO[A]
  val F: IO.type = IO

  override def tests: List[Test] = List(
    property("test Success log - with batch param rendering", testSuccessWithBatchParamRendering),
    property("test Success log - without batch param rendering", testSuccessWithoutBatchParamRendering),
    property("test ExecFailure", testExecFailure),
    property("test ProcessingFailure", testProcessingFailure),
  )

  def testSuccessWithBatchParamRendering: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              loggerf.Level.info,
              s"""Successful Statement Execution:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = ${args.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")}
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (${(exec + processing).toString} ms total)
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .withBatchParamRenderingWhenSuccessful[F]
        .run(
          Success(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            Parameters.Batch(() => args),
            label,
            exec.milliseconds,
            processing.milliseconds,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testSuccessWithoutBatchParamRendering: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              loggerf.Level.info,
              s"""Successful Statement Execution:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = <batch arguments not rendered>
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (${(exec + processing).toString} ms total)
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .withoutBatchParamRenderingWhenSuccessful[F]
        .run(
          Success(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            Parameters.Batch(() => args),
            label,
            exec.milliseconds,
            processing.milliseconds,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testExecFailure: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      errMessage <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("errMessage")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val expectedException = new RuntimeException(errMessage)

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              loggerf.Level.error,
              s"""Failed Statement Execution:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = ${args.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")}
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec (failed)
                 |    failure = ${expectedException.getMessage}
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .withBatchParamRenderingWhenSuccessful[F]
        .run(
          ExecFailure(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            Parameters.Batch(() => args),
            label,
            exec.milliseconds,
            expectedException,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testProcessingFailure: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
      errMessage <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("errMessage")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val expectedException = new RuntimeException(errMessage)

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              loggerf.Level.error,
              s"""Failed Resultset Processing:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = ${args.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")}
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (failed) (${(exec + processing).toString} ms total)
                 |    failure = ${expectedException.getMessage}
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .withBatchParamRenderingWhenSuccessful[F]
        .run(
          ProcessingFailure(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            Parameters.Batch(() => args),
            label,
            exec.milliseconds,
            processing.milliseconds,
            expectedException,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

}
