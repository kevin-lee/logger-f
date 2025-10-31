package loggerf.doobie1

import cats.effect._
import cats.syntax.all._
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
    property(
      "test the default LoggerFLogHandler[F] for Success log - with batch param rendering",
      testDefaultLoggerFLogHandlerSuccessWithBatchParamRendering,
    ),
    property(
      "test the default LoggerFLogHandler[F] for Success log - without batch param rendering",
      testDefaultLoggerFLogHandlerSuccessWithoutBatchParamRendering,
    ),
    property(
      "test LoggerFLogHandler[F] with custom log level for Success log - with batch param rendering",
      testLoggerFLogHandlerCustomLogLevelForSuccessWithBatchParamRendering,
    ),
    property(
      "test LoggerFLogHandler[F] with custom log level for Success log - without batch param rendering",
      testLoggerFLogHandlerCustomLogLevelForSuccessWithoutBatchParamRendering,
    ),
    /////
    property("test the default LoggerFLogHandler[F] for ExecFailure", testDefaultLoggerFLogHandlerExecFailure),
    property(
      "test LoggerFLogHandler[F] custom log level for ExecFailure",
      testDefaultLoggerFLogHandlerCustomLogLevelForExecFailure,
    ),
    /////
    property(
      "test the default LoggerFLogHandler[F] for ProcessingFailure",
      testDefaultLoggerFLogHandlerProcessingFailure,
    ),
    property(
      "test LoggerFLogHandler[F] custom log level for ProcessingFailure",
      testDefaultLoggerFLogHandlerCustomLogLevelForProcessingFailure,
    ),
    /////
    property(
      "test LoggerFLogHandler[F].toString",
      testLoggerFLogHandlerToString,
    ),
    /////
    property(
      "test Show[LoggerFLogHandler.BatchParamRenderingWhenSuccessful]",
      testLoggerFLogHandlerShowBatchParamRenderingWhenSuccessful,
    ),
    property(
      "test Eq[LoggerFLogHandler.BatchParamRenderingWhenSuccessful]",
      testLoggerFLogHandlerEqBatchParamRenderingWhenSuccessful,
    ),
  )

  def testDefaultLoggerFLogHandlerSuccessWithBatchParamRendering: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val (params, paramsStr) = toParamsAndParamsStr(args, LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render)

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
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (${(exec + processing).toString} ms total)
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .defaultWithBatchParamRenderingWhenSuccessful[F]
        .run(
          Success(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
            label,
            exec.milliseconds,
            processing.milliseconds,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testDefaultLoggerFLogHandlerSuccessWithoutBatchParamRendering: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val (params, paramsStr) =
        toParamsAndParamsStr(args, LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender)

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
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (${(exec + processing).toString} ms total)
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .defaultWithoutBatchParamRenderingWhenSuccessful[F]
        .run(
          Success(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
            label,
            exec.milliseconds,
            processing.milliseconds,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testLoggerFLogHandlerCustomLogLevelForSuccessWithBatchParamRendering: Property =
    for {
      logLevel <-
        Gen.element1(loggerf.Level.debug, loggerf.Level.info, loggerf.Level.warn, loggerf.Level.error).log("logLevel")
      columns  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table    <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val renderBatchParams   = LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render
      val (params, paramsStr) = toParamsAndParamsStr(args, renderBatchParams)

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              logLevel,
              s"""Successful Statement Execution:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (${(exec + processing).toString} ms total)
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler[F](
        renderBatchParams,
        successfulLogLevel = LoggerFLogHandler.SuccessfulLogLevel(logLevel),
      )
        .run(
          Success(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
            label,
            exec.milliseconds,
            processing.milliseconds,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testLoggerFLogHandlerCustomLogLevelForSuccessWithoutBatchParamRendering: Property =
    for {
      logLevel <-
        Gen.element1(loggerf.Level.debug, loggerf.Level.info, loggerf.Level.warn, loggerf.Level.error).log("logLevel")
      columns  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table    <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val renderBatchParams   = LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender
      val (params, paramsStr) = toParamsAndParamsStr(args, renderBatchParams)

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              logLevel,
              s"""Successful Statement Execution:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (${(exec + processing).toString} ms total)
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler[F](
        renderBatchParams,
        successfulLogLevel = LoggerFLogHandler.SuccessfulLogLevel(logLevel),
      )
        .run(
          Success(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
            label,
            exec.milliseconds,
            processing.milliseconds,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testDefaultLoggerFLogHandlerExecFailure: Property =
    for {
      columns <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table   <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      errMessage <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("errMessage")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val (params, paramsStr) = toParamsAndParamsStr(args, LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render)

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
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec (failed)
                 |    failure = ${expectedException.getMessage}
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .defaultWithBatchParamRenderingWhenSuccessful[F]
        .run(
          ExecFailure(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
            label,
            exec.milliseconds,
            expectedException,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testDefaultLoggerFLogHandlerCustomLogLevelForExecFailure: Property =
    for {
      logLevel <-
        Gen.element1(loggerf.Level.debug, loggerf.Level.info, loggerf.Level.warn, loggerf.Level.error).log("logLevel")
      columns  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table    <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      errMessage <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("errMessage")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val (params, paramsStr) = toParamsAndParamsStr(args, LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render)

      val expectedException = new RuntimeException(errMessage)

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              logLevel,
              s"""Failed Statement Execution:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec (failed)
                 |    failure = ${expectedException.getMessage}
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler[F](
        LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
        execFailureLogLevel = LoggerFLogHandler.ExecFailureLogLevel(logLevel),
      )
        .run(
          ExecFailure(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
            label,
            exec.milliseconds,
            expectedException,
          )
        ) *> F {
        val actual = canLog.getOrderedMessages
        actual ==== expected
      }
    }

  def testDefaultLoggerFLogHandlerProcessingFailure: Property =
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

      val (params, paramsStr) = toParamsAndParamsStr(args, LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render)

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
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (failed) (${(exec + processing).toString} ms total)
                 |    failure = ${expectedException.getMessage}
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler
        .defaultWithBatchParamRenderingWhenSuccessful[F]
        .run(
          ProcessingFailure(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
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

  def testDefaultLoggerFLogHandlerCustomLogLevelForProcessingFailure: Property =
    for {
      logLevel <-
        Gen.element1(loggerf.Level.debug, loggerf.Level.info, loggerf.Level.warn, loggerf.Level.error).log("logLevel")
      columns  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(1, 5)).log("columns")
      table    <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("table")
      args  <- Gen.string(Gen.alpha, Range.linear(3, 10)).list(Range.linear(0, 5)).list(Range.linear(0, 5)).log("args")
      label <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("label")
      exec  <- Gen.int(Range.linear(10, 3000)).log("exec")
      processing <- Gen.int(Range.linear(10, 3000)).log("processing")
      errMessage <- Gen.string(Gen.alpha, Range.linear(3, 10)).log("errMessage")
    } yield runIO {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      val (params, paramsStr) = toParamsAndParamsStr(args, LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render)

      val expectedException = new RuntimeException(errMessage)

      val expected =
        OrderedMessages(
          Vector(
            (
              0,
              logLevel,
              s"""Failed Resultset Processing:
                 |
                 |  SELECT ${columns.mkString(", ")} FROM $table
                 |
                 | parameters = $paramsStr
                 |      label = $label
                 |    elapsed = ${exec.toString} ms exec + ${processing.toString} ms processing (failed) (${(exec + processing).toString} ms total)
                 |    failure = ${expectedException.getMessage}
                 |""".stripMargin,
            )
          )
        )

      LoggerFLogHandler[F](
        LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
        processingFailureLogLevel = LoggerFLogHandler.ProcessingFailureLogLevel(logLevel),
      )
        .run(
          ProcessingFailure(
            s"SELECT ${columns.mkString(", ")} FROM $table",
            params,
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

  def testLoggerFLogHandlerToString: Property = {
    for {
      successfulBatchParamRendering <- Gen
                                         .element1(
                                           LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
                                           LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender,
                                         )
                                         .log("successfulBatchParamRendering")
      logLevelForSuccess            <- Gen
                                         .element1(
                                           loggerf.Level.debug,
                                           loggerf.Level.info,
                                           loggerf.Level.warn,
                                           loggerf.Level.error,
                                         )
                                         .log("logLevelForSuccess")
      logLevelForProcessingFailure  <- Gen
                                         .element1(
                                           loggerf.Level.debug,
                                           loggerf.Level.info,
                                           loggerf.Level.warn,
                                           loggerf.Level.error,
                                         )
                                         .log("logLevelForProcessingFailure")
      logLevelForExecFailure        <- Gen
                                         .element1(
                                           loggerf.Level.debug,
                                           loggerf.Level.info,
                                           loggerf.Level.warn,
                                           loggerf.Level.error,
                                         )
                                         .log("logLevelForExecFailure")
    } yield {
      implicit val canLog: CanLog4Testing = CanLog4Testing()

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val expected =
        s"""LoggerFLogHandlerF(
           |  successfulBatchParamRendering = ${successfulBatchParamRendering.show}
           |  successfulLogLevel = ${logLevelForSuccess.toString}
           |  processingFailureLogLevel = ${logLevelForProcessingFailure.toString}
           |  execFailureLogLevel = ${logLevelForExecFailure.toString}
           |)""".stripMargin

      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val actual = LoggerFLogHandler[F](
        successfulBatchParamRendering,
        LoggerFLogHandler.SuccessfulLogLevel(logLevelForSuccess),
        LoggerFLogHandler.ProcessingFailureLogLevel(logLevelForProcessingFailure),
        LoggerFLogHandler.ExecFailureLogLevel(logLevelForExecFailure),
      ).toString

      actual ==== expected
    }
  }

  def testLoggerFLogHandlerShowBatchParamRenderingWhenSuccessful: Property = for {
    batchParamRenderingWhenSuccessful <- Gen
                                           .element1(
                                             LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
                                             LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender,
                                           )
                                           .log("batchParamRenderingWhenSuccessful")
  } yield {
    val expected = batchParamRenderingWhenSuccessful match {
      case LoggerFLogHandler.BatchParamRenderingWhenSuccessful.Render =>
        "Render batch parameters"
      case LoggerFLogHandler.BatchParamRenderingWhenSuccessful.DoNotRender =>
        "Do NOT render batch parameters"
    }

    val actual = batchParamRenderingWhenSuccessful.show
    actual ==== expected
  }

  def testLoggerFLogHandlerEqBatchParamRenderingWhenSuccessful: Property = for {
    batchParamRenderingWhenSuccessful <- Gen
                                           .element1(
                                             LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
                                             LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender,
                                           )
                                           .log("batchParamRenderingWhenSuccessful")
  } yield {
    val (expectedEqualValue, expectedNotEqualValue) = batchParamRenderingWhenSuccessful match {
      case LoggerFLogHandler.BatchParamRenderingWhenSuccessful.Render =>
        (
          LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
          LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender,
        )
      case LoggerFLogHandler.BatchParamRenderingWhenSuccessful.DoNotRender =>
        (
          LoggerFLogHandler.BatchParamRenderingWhenSuccessful.doNotRender,
          LoggerFLogHandler.BatchParamRenderingWhenSuccessful.render,
        )
    }

    Result.all(
      List(
        Result.diffNamed(
          show"$batchParamRenderingWhenSuccessful === $expectedEqualValue",
          batchParamRenderingWhenSuccessful,
          expectedEqualValue,
        )(_ === _),
        Result.diffNamed(
          show"$batchParamRenderingWhenSuccessful =!= $expectedNotEqualValue",
          batchParamRenderingWhenSuccessful,
          expectedNotEqualValue,
        )(_ =!= _),
      )
    )

  }

  @SuppressWarnings(Array("org.wartremover.warts.SizeIs"))
  private def toParamsAndParamsStr(
    args: List[List[String]],
    batchParamRenderingWhenSuccessful: LoggerFLogHandler.BatchParamRenderingWhenSuccessful,
  ): (Parameters, String) = {
    (
      args
        .headOption
        .fold[Parameters](Parameters.NonBatch(List.empty))(firstParams =>
          if (args.length === 1) Parameters.NonBatch(firstParams) else Parameters.Batch(() => args)
        ),
      (batchParamRenderingWhenSuccessful, args.length) match {
        case (LoggerFLogHandler.BatchParamRenderingWhenSuccessful.Render, 0 | 1) =>
          args.headOption.fold("[]")(_.mkString("[", ", ", "]"))

        case (LoggerFLogHandler.BatchParamRenderingWhenSuccessful.Render, _) =>
          args.map(_.mkString("[", ", ", "]")).mkString("[\n   ", ",\n   ", "\n ]")

        case (LoggerFLogHandler.BatchParamRenderingWhenSuccessful.DoNotRender, 0 | 1) =>
          args.headOption.fold("[]")(_.mkString("[", ", ", "]"))

        case (LoggerFLogHandler.BatchParamRenderingWhenSuccessful.DoNotRender, _) =>
          "<batch arguments not rendered>"
      },
    )
  }
}
