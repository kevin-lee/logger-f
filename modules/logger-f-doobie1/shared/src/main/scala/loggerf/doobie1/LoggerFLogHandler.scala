package loggerf.doobie1

import cats.syntax.all._
import doobie.util.log
import doobie.util.log.LogHandler
import loggerf.core.Log
import loggerf.syntax.all._

/** @author Kevin Lee
  * @since 2023-07-28
  */
object LoggerFLogHandler {
  def apply[F[*]: Log]: LogHandler[F] = new LoggerFLogHandlerF[F]

  // format: off
  final private class LoggerFLogHandlerF[F[*]: Log] extends LogHandler[F] {
    override def run(logEvent: log.LogEvent): F[Unit] = logEvent match {
      case log.Success(sql, args, label, e1, e2) =>
        logS_(
          show"""Successful Statement Execution:
                |
                |  ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | arguments = ${args.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")}
                | label     = $label
                |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (${(e1 + e2).toMillis} ms total)
                |""".stripMargin
        )(info)

      case log.ProcessingFailure(sql, args, label, e1, e2, failure) =>
        logS_(
          show"""Failed Resultset Processing:
                |
                |  ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | arguments = ${args.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")}
                | label     = $label
                |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
                |   failure = ${failure.getMessage}
                |""".stripMargin
        )(error)

      case log.ExecFailure(sql, args, label, e1, failure) =>
        logS_(
          show"""Failed Statement Execution:
                |
                |  ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | arguments = ${args.allParams.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")}
                | label     = $label
                |   elapsed = ${e1.toMillis} ms exec (failed)
                |   failure = ${failure.getMessage}
                |""".stripMargin
        )(error)
    }
  }
  // format: on
}
