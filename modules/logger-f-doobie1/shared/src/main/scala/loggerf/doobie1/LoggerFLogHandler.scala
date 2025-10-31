package loggerf.doobie1

import cats.syntax.all._
import doobie.util.log
import doobie.util.log.{LogHandler, Parameters}
import loggerf.core.Log
import loggerf.syntax.all._

/** @author Kevin Lee
  * @since 2023-07-28
  */
object LoggerFLogHandler {
  private def apply[F[*]: Log](batchParamRenderingWhenSuccessful: BatchParamRenderingWhenSuccessful): LogHandler[F] =
    new LoggerFLogHandlerF[F](batchParamRenderingWhenSuccessful)

  def withBatchParamRenderingWhenSuccessful[F[*]: Log]: LogHandler[F] = apply(BatchParamRenderingWhenSuccessful.render)

  def withoutBatchParamRenderingWhenSuccessful[F[*]: Log]: LogHandler[F] =
    apply(BatchParamRenderingWhenSuccessful.doNotRender)

  // format: off
  final private class LoggerFLogHandlerF[F[*]: Log](val successfulBatchParamRendering: BatchParamRenderingWhenSuccessful) extends LogHandler[F] {

    val renderSuccessfulBatchParams: Parameters => String = successfulBatchParamRendering match {
      case BatchParamRenderingWhenSuccessful.Render =>
        (params: Parameters) => params.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")

      case BatchParamRenderingWhenSuccessful.DoNotRender =>
        _ => "<batch arguments not rendered>"
    }

    override def run(logEvent: log.LogEvent): F[Unit] = logEvent match {
      case log.Success(sql, params, label, e1, e2) =>
        val paramsStr = params match {
          case nonBatch: Parameters.NonBatch => s"[${nonBatch.paramsAsList.mkString(", ")}]"
          case _: Parameters.Batch           => renderSuccessfulBatchParams(params)
        }
        logS_(
          show"""Successful Statement Execution:
                |
                |  ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | parameters = $paramsStr
                |      label = $label
                |    elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (${(e1 + e2).toMillis} ms total)
                |""".stripMargin
        )(info)

      case log.ProcessingFailure(sql, params, label, e1, e2, failure) =>
        val paramsStr = params match {
          case nonBatch: Parameters.NonBatch => s"[${nonBatch.paramsAsList.mkString(", ")}]"
          case _: Parameters.Batch           => params.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")
        }
        logS_(
          show"""Failed Resultset Processing:
                |
                |  ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | parameters = $paramsStr
                |      label = $label
                |    elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
                |    failure = ${failure.getMessage}
                |""".stripMargin
        )(error)

      case log.ExecFailure(sql, params, label, e1, failure) =>
        val paramsStr = params match {
          case nonBatch: Parameters.NonBatch => s"[${nonBatch.paramsAsList.mkString(", ")}]"
          case _: Parameters.Batch           => params.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")
        }
        logS_(
          show"""Failed Statement Execution:
                |
                |  ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | parameters = $paramsStr
                |      label = $label
                |    elapsed = ${e1.toMillis} ms exec (failed)
                |    failure = ${failure.getMessage}
                |""".stripMargin
        )(error)
    }
  }
  // format: on

  sealed trait BatchParamRenderingWhenSuccessful
  object BatchParamRenderingWhenSuccessful {
    case object Render extends BatchParamRenderingWhenSuccessful
    case object DoNotRender extends BatchParamRenderingWhenSuccessful

    def render: BatchParamRenderingWhenSuccessful      = Render
    def doNotRender: BatchParamRenderingWhenSuccessful = DoNotRender

  }

}
