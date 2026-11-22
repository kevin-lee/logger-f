package loggerf.logger

import hedgehog._
import hedgehog.runner._
import loggerf.SourceLocation
import loggerf.logger.RecordingLog4jLogger.Record
import loggerf.test_data.{ExpectedSourceLocationRendering, Gens}
import org.apache.logging.log4j.{Level => Log4jLevel}

import java.util.concurrent.atomic.AtomicInteger

/** @author Kevin Lee
  * @since 2026-11-22
  */
object Log4jLoggerSpec extends Properties {

  override def tests: List[Test] = List(
    property(
      "the methods without SourceLocation log the message as it is at the right level",
      testWithoutSourceLocation,
    ),
    property(
      "the methods without SourceLocation and with Throwable log the message and the Throwable at the right level",
      testWithoutSourceLocationWithThrowable,
    ),
    property(
      "the location-aware methods render the location with the given sourceLocationFormat",
      testWithSourceLocation,
    ),
    property(
      "the location-aware methods with Throwable render the location with the given sourceLocationFormat",
      testWithSourceLocationAndThrowable,
    ),
    example(
      "the location-aware methods use SourceLocationFormat.default if no sourceLocationFormat is given",
      testDefaultSourceLocationFormat,
    ),
    example("a disabled level does not evaluate the message", testDisabledLevelsAreLazy),
    example("an enabled level evaluates the message once", testEnabledLevelsEvaluateMessageOnce),
    example("only the enabled levels log", testOnlyEnabledLevelsLog),
    example("withoutSourceLocation drops the location", testWithoutSourceLocationDropsLocation),
    example(
      "the constructor and the factory methods without SourceLocationFormat use SourceLocationFormat.default",
      testFactoriesWithoutSourceLocationFormat,
    ),
    property(
      "the constructor and the factory methods with SourceLocationFormat use the given SourceLocationFormat",
      testFactoriesWithSourceLocationFormat,
    ),
    example(
      "log4jCanLogWithLoggerAndSourceLocationFormat logs with the given Logger",
      testWithLoggerAndSourceLocationFormatUsesGivenLogger,
    ),
    example(
      "the factory methods with a class or a name get the Logger with the right name",
      testFactoriesUseTheRightLoggerName,
    ),
  )

  private val loc: SourceLocation = SourceLocation("com.example.Foo", "bar", "Foo.scala", 42)

  def testWithoutSourceLocation: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger = RecordingLog4jLogger.allEnabled()
    val canLog = new Log4jLogger(logger, sourceLocationFormat)
    canLog.debug(message)
    canLog.info(message)
    canLog.warn(message)
    canLog.error(message)
    logger.records ==== Vector(
      Record(Log4jLevel.DEBUG, message, None),
      Record(Log4jLevel.INFO, message, None),
      Record(Log4jLevel.WARN, message, None),
      Record(Log4jLevel.ERROR, message, None),
    )
  }

  def testWithoutSourceLocationWithThrowable: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger    = RecordingLog4jLogger.allEnabled()
    val canLog    = new Log4jLogger(logger, sourceLocationFormat)
    val throwable = new RuntimeException("t")
    canLog.debug(throwable)(message)
    canLog.info(throwable)(message)
    canLog.warn(throwable)(message)
    canLog.error(throwable)(message)
    logger.records ==== Vector(
      Record(Log4jLevel.DEBUG, message, Some(throwable)),
      Record(Log4jLevel.INFO, message, Some(throwable)),
      Record(Log4jLevel.WARN, message, Some(throwable)),
      Record(Log4jLevel.ERROR, message, Some(throwable)),
    )
  }

  def testWithSourceLocation: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    sourceLocation       <- Gens.genSourceLocation.log("sourceLocation")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger = RecordingLog4jLogger.allEnabled()
    val canLog = new Log4jLogger(logger, sourceLocationFormat)
    canLog.debug(sourceLocation)(message)
    canLog.info(sourceLocation)(message)
    canLog.warn(sourceLocation)(message)
    canLog.error(sourceLocation)(message)

    val expected = ExpectedSourceLocationRendering.render(sourceLocationFormat, message, sourceLocation)
    logger.records ==== Vector(
      Record(Log4jLevel.DEBUG, expected, None),
      Record(Log4jLevel.INFO, expected, None),
      Record(Log4jLevel.WARN, expected, None),
      Record(Log4jLevel.ERROR, expected, None),
    )
  }

  def testWithSourceLocationAndThrowable: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    sourceLocation       <- Gens.genSourceLocation.log("sourceLocation")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger    = RecordingLog4jLogger.allEnabled()
    val canLog    = new Log4jLogger(logger, sourceLocationFormat)
    val throwable = new RuntimeException("t")
    canLog.debug(throwable, sourceLocation)(message)
    canLog.info(throwable, sourceLocation)(message)
    canLog.warn(throwable, sourceLocation)(message)
    canLog.error(throwable, sourceLocation)(message)

    val expected = ExpectedSourceLocationRendering.render(sourceLocationFormat, message, sourceLocation)
    logger.records ==== Vector(
      Record(Log4jLevel.DEBUG, expected, Some(throwable)),
      Record(Log4jLevel.INFO, expected, Some(throwable)),
      Record(Log4jLevel.WARN, expected, Some(throwable)),
      Record(Log4jLevel.ERROR, expected, Some(throwable)),
    )
  }

  def testDefaultSourceLocationFormat: Result = {
    val logger1   = RecordingLog4jLogger.allEnabled()
    val logger2   = RecordingLog4jLogger.allEnabled()
    val throwable = new RuntimeException("t")

    new Log4jLogger(logger1).info(loc)("i")
    Log4jLogger.log4jCanLogWith(logger2).error(throwable, loc)("e")

    Result.all(
      List(
        logger1.records ==== Vector(Record(Log4jLevel.INFO, "i at com.example.Foo.bar(Foo.scala:42)", None)),
        logger2.records ==== Vector(
          Record(Log4jLevel.ERROR, "e at com.example.Foo.bar(Foo.scala:42)", Some(throwable))
        ),
      )
    )
  }

  /** Calls all 16 methods once. */
  private def callAllMethods(canLog: CanLog, message: () => String): Unit = {
    val throwable = new RuntimeException("t")
    canLog.debug(message())
    canLog.debug(throwable)(message())
    canLog.debug(loc)(message())
    canLog.debug(throwable, loc)(message())
    canLog.info(message())
    canLog.info(throwable)(message())
    canLog.info(loc)(message())
    canLog.info(throwable, loc)(message())
    canLog.warn(message())
    canLog.warn(throwable)(message())
    canLog.warn(loc)(message())
    canLog.warn(throwable, loc)(message())
    canLog.error(message())
    canLog.error(throwable)(message())
    canLog.error(loc)(message())
    canLog.error(throwable, loc)(message())
  }

  private def countingMessage(counter: AtomicInteger): () => String = { () =>
    counter.incrementAndGet()
    "message"
  }

  def testDisabledLevelsAreLazy: Result = {
    val counter = new AtomicInteger(0)
    val logger  = RecordingLog4jLogger.allDisabled()
    callAllMethods(new Log4jLogger(logger, SourceLocationFormat.beforeMessage("[", "] ")), countingMessage(counter))
    Result.all(
      List(
        counter.get() ==== 0,
        logger.records ==== Vector.empty[Record],
      )
    )
  }

  def testEnabledLevelsEvaluateMessageOnce: Result = {
    val counter = new AtomicInteger(0)
    val logger  = RecordingLog4jLogger.allEnabled()
    callAllMethods(new Log4jLogger(logger), countingMessage(counter))
    Result.all(
      List(
        counter.get() ==== 16,
        logger.records.length ==== 16,
      )
    )
  }

  def testOnlyEnabledLevelsLog: Result = {
    val logger = new RecordingLog4jLogger(Log4jLevel.WARN)
    callAllMethods(new Log4jLogger(logger), () => "m")
    logger.records.map(_.level).distinct ==== Vector(Log4jLevel.WARN, Log4jLevel.ERROR)
  }

  def testFactoriesUseTheRightLoggerName: Result = {
    val canLogs: List[(String, CanLog, String)] = List(
      (
        "Log4jLogger.log4jCanLog[A]",
        Log4jLogger.log4jCanLog[RecordingLog4jLogger],
        classOf[RecordingLog4jLogger].getName,
      ),
      ("Log4jLogger.log4jCanLog(name)", Log4jLogger.log4jCanLog("test-logger"), "test-logger"),
      (
        "Log4jLogger.log4jCanLogWithSourceLocationFormat[A](sourceLocationFormat)",
        Log4jLogger.log4jCanLogWithSourceLocationFormat[RecordingLog4jLogger](SourceLocationFormat.default),
        classOf[RecordingLog4jLogger].getName,
      ),
      (
        "Log4jLogger.log4jCanLogWithSourceLocationFormat(name, sourceLocationFormat)",
        Log4jLogger.log4jCanLogWithSourceLocationFormat("test-logger-2", SourceLocationFormat.default),
        "test-logger-2",
      ),
    )
    Result.all(
      canLogs.map {
        case (name, canLog, expectedLoggerName) =>
          (canLog match {
            case log4jLogger: Log4jLogger =>
              log4jLogger.logger.getName ==== expectedLoggerName
            case other =>
              Result.failure.log(s"Expected Log4jLogger but got ${other.getClass.getName}")
          }).log(name)
      }
    )
  }

  def testWithoutSourceLocationDropsLocation: Result = {
    val logger    = RecordingLog4jLogger.allEnabled()
    val canLog    = new Log4jLogger(logger, SourceLocationFormat.beforeMessage("[", "] ")).withoutSourceLocation
    val throwable = new RuntimeException("t")
    canLog.debug(loc)("d")
    canLog.info(throwable, loc)("i")
    canLog.warn(loc)("w")
    canLog.error(throwable, loc)("e")
    logger.records ==== Vector(
      Record(Log4jLevel.DEBUG, "d", None),
      Record(Log4jLevel.INFO, "i", Some(throwable)),
      Record(Log4jLevel.WARN, "w", None),
      Record(Log4jLevel.ERROR, "e", Some(throwable)),
    )
  }

  def testFactoriesWithoutSourceLocationFormat: Result = {
    val logger                          = RecordingLog4jLogger.allEnabled()
    val canLogs: List[(String, CanLog)] = List(
      "new Log4jLogger(logger)"             -> new Log4jLogger(logger),
      "Log4jLogger.log4jCanLogWith(logger)" -> Log4jLogger.log4jCanLogWith(logger),
      "Log4jLogger.log4jCanLog[A]"          -> Log4jLogger.log4jCanLog[RecordingLog4jLogger],
      "Log4jLogger.log4jCanLog(name)"       -> Log4jLogger.log4jCanLog("test-logger"),
    )
    Result.all(
      canLogs.map {
        case (name, canLog) =>
          (canLog.sourceLocationFormat ==== SourceLocationFormat.default).log(name)
      }
    )
  }

  def testFactoriesWithSourceLocationFormat: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
  } yield {
    val logger                          = RecordingLog4jLogger.allEnabled()
    val canLogs: List[(String, CanLog)] = List(
      "new Log4jLogger(logger, sourceLocationFormat)"                                          ->
        new Log4jLogger(logger, sourceLocationFormat),
      "Log4jLogger.log4jCanLogWithSourceLocationFormat[A](sourceLocationFormat)"               ->
        Log4jLogger.log4jCanLogWithSourceLocationFormat[RecordingLog4jLogger](sourceLocationFormat),
      "Log4jLogger.log4jCanLogWithSourceLocationFormat(name, sourceLocationFormat)"            ->
        Log4jLogger.log4jCanLogWithSourceLocationFormat("test-logger", sourceLocationFormat),
      "Log4jLogger.log4jCanLogWithLoggerAndSourceLocationFormat(logger, sourceLocationFormat)" ->
        Log4jLogger.log4jCanLogWithLoggerAndSourceLocationFormat(logger, sourceLocationFormat),
    )
    Result.all(
      canLogs.map {
        case (name, canLog) =>
          (canLog.sourceLocationFormat ==== sourceLocationFormat).log(name)
      }
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testWithLoggerAndSourceLocationFormatUsesGivenLogger: Result = {
    val logger = RecordingLog4jLogger.allEnabled()
    val canLog =
      Log4jLogger.log4jCanLogWithLoggerAndSourceLocationFormat(logger, SourceLocationFormat.beforeMessage("[", "] "))
    canLog.info(loc)("i")
    Result.all(
      List(
        canLog match {
          case log4jLogger: Log4jLogger =>
            Result.assert(log4jLogger.logger eq logger).log("Log4jLogger.logger should be the given Logger")
          case other =>
            Result.failure.log(s"Expected Log4jLogger but got ${other.getClass.getName}")
        },
        logger.records ==== Vector(Record(Log4jLevel.INFO, "[com.example.Foo.bar(Foo.scala:42)] i", None)),
      )
    )
  }

}
