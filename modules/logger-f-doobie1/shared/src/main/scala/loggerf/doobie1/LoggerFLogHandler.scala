package loggerf.doobie1

import cats.syntax.all._
import cats.{Eq, Show}
import doobie.util.log
import doobie.util.log.{LogHandler, Parameters}
import loggerf.core.Log
import loggerf.syntax.all._

/** @author Kevin Lee
  * @since 2023-07-28
  */
object LoggerFLogHandler {
  def apply[F[*]: Log](
    batchParamRenderingWhenSuccessful: BatchParamRenderingWhenSuccessful,
    successfulLogLevel: SuccessfulLogLevel = DefaultSuccessfulLogLevel, // scalafix:ok DisableSyntax.defaultArgs
    processingFailureLogLevel: ProcessingFailureLogLevel =
      DefaultProcessingFailureLogLevel, // scalafix:ok DisableSyntax.defaultArgs
    execFailureLogLevel: ExecFailureLogLevel = DefaultExecFailureLogLevel, // scalafix:ok DisableSyntax.defaultArgs
  ): LogHandler[F] =
    new LoggerFLogHandlerF[F](
      batchParamRenderingWhenSuccessful,
      successfulLogLevel.successfulLogLevel,
      processingFailureLogLevel.processingFailureLogLevel,
      execFailureLogLevel.execFailureLogLevel,
    )

  def defaultWithBatchParamRenderingWhenSuccessful[F[*]: Log]: LogHandler[F] =
    apply(
      BatchParamRenderingWhenSuccessful.render,
      DefaultSuccessfulLogLevel,
      DefaultProcessingFailureLogLevel,
      DefaultExecFailureLogLevel,
    )

  def defaultWithoutBatchParamRenderingWhenSuccessful[F[*]: Log]: LogHandler[F] =
    apply(
      BatchParamRenderingWhenSuccessful.doNotRender,
      DefaultSuccessfulLogLevel,
      DefaultProcessingFailureLogLevel,
      DefaultExecFailureLogLevel,
    )

  // format: off
  final private class LoggerFLogHandlerF[F[*]: Log](
    private val successfulBatchParamRendering: BatchParamRenderingWhenSuccessful,
    private val _successfulLogLevel: loggerf.Level,
    private val _processingFailureLogLevel: loggerf.Level,
    private val _execFailureLogLevel: loggerf.Level,
  ) extends LogHandler[F] {

    private val getLeveledMessageFunction = (logLevel: loggerf.Level) => logLevel match {
      case loggerf.Level.Debug => loggerf.core.syntax.LogMessageSyntax.debug
      case loggerf.Level.Info => loggerf.core.syntax.LogMessageSyntax.info
      case loggerf.Level.Warn => loggerf.core.syntax.LogMessageSyntax.warn
      case loggerf.Level.Error => loggerf.core.syntax.LogMessageSyntax.error
    }
    
    private val successfulLogLevel = getLeveledMessageFunction(_successfulLogLevel)
    private val processingFailureLogLevel = getLeveledMessageFunction(_processingFailureLogLevel)
    private val execFailureLogLevel = getLeveledMessageFunction(_execFailureLogLevel)

    val renderSuccessfulBatchParams: Parameters => String = successfulBatchParamRendering match {
      case BatchParamRenderingWhenSuccessful.Render =>
        (params: Parameters) => params.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")

      case BatchParamRenderingWhenSuccessful.DoNotRender =>
        _ => "<batch arguments not rendered>"
    }

    override def run(logEvent: log.LogEvent): F[Unit] = logEvent match {
      case log.Success(sql, params, label, e1, e2) =>
        val paramsStr = params match {
          case Parameters.NonBatch(paramsAsList) => s"[${paramsAsList.mkString(", ")}]"
          case Parameters.Batch(_) => renderSuccessfulBatchParams(params)
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
        )(successfulLogLevel)

      case log.ProcessingFailure(sql, params, label, e1, e2, failure) =>
        val paramsStr = params match {
          case Parameters.NonBatch(paramsAsList) => s"[${paramsAsList.mkString(", ")}]"
          case Parameters.Batch(_) => params.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")
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
        )(processingFailureLogLevel)

      case log.ExecFailure(sql, params, label, e1, failure) =>
        val paramsStr = params match {
          case Parameters.NonBatch(paramsAsList) => s"[${paramsAsList.mkString(", ")}]"
          case Parameters.Batch(_) => params.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")
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
        )(execFailureLogLevel)
    }

    @SuppressWarnings(Array("org.wartremover.warts.ToString"))
    override val toString: String = {
      s"""LoggerFLogHandlerF(
         |  successfulBatchParamRendering = ${successfulBatchParamRendering.show}
         |  successfulLogLevel = ${_successfulLogLevel.toString}
         |  processingFailureLogLevel = ${_processingFailureLogLevel.toString}
         |  execFailureLogLevel = ${_execFailureLogLevel.toString}
         |)""".stripMargin
    }
  }
  // format: on

  sealed trait BatchParamRenderingWhenSuccessful
  object BatchParamRenderingWhenSuccessful {
    case object Render extends BatchParamRenderingWhenSuccessful
    case object DoNotRender extends BatchParamRenderingWhenSuccessful

    def render: BatchParamRenderingWhenSuccessful      = Render
    def doNotRender: BatchParamRenderingWhenSuccessful = DoNotRender

    implicit val showBatchParamRenderingWhenSuccessful: Show[BatchParamRenderingWhenSuccessful] = {
      case BatchParamRenderingWhenSuccessful.Render => "Render batch parameters"
      case BatchParamRenderingWhenSuccessful.DoNotRender => "Do NOT render batch parameters"
    }

    implicit val batchParamRenderingWhenSuccessfulEq: Eq[BatchParamRenderingWhenSuccessful] =
      Eq.fromUniversalEquals[BatchParamRenderingWhenSuccessful]
  }

  final case class SuccessfulLogLevel(successfulLogLevel: loggerf.Level) extends AnyVal
  val DefaultSuccessfulLogLevel: SuccessfulLogLevel = SuccessfulLogLevel(loggerf.Level.info)

  final case class ProcessingFailureLogLevel(processingFailureLogLevel: loggerf.Level) extends AnyVal
  val DefaultProcessingFailureLogLevel: ProcessingFailureLogLevel = ProcessingFailureLogLevel(loggerf.Level.error)

  final case class ExecFailureLogLevel(execFailureLogLevel: loggerf.Level) extends AnyVal
  val DefaultExecFailureLogLevel: ExecFailureLogLevel = ExecFailureLogLevel(loggerf.Level.error)

}
