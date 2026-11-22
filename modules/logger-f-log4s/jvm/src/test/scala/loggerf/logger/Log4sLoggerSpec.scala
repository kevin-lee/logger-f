package loggerf.logger

import hedgehog._
import hedgehog.runner._
import loggerf.SourceLocation
import loggerf.logger.RecordingSlf4jLogger.Record
import loggerf.test_data.{ExpectedSourceLocationRendering, Gens}
import org.slf4j.event.{Level => Slf4jLevel}

import java.util.concurrent.atomic.AtomicInteger

/** @author Kevin Lee
  * @since 2026-11-22
  */
object Log4sLoggerSpec extends Properties {

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
      "log4sCanLogWithLoggerAndSourceLocationFormat logs with the given Logger",
      testWithLoggerAndSourceLocationFormatUsesGivenLogger,
    ),
  )

  private val loc: SourceLocation = SourceLocation("com.example.Foo", "bar", "Foo.scala", 42)

  /** On the JVM, a log4s `Logger` wraps an slf4j `Logger`. */
  private def log4s(logger: RecordingSlf4jLogger): org.log4s.Logger = new org.log4s.Logger(logger)

  def testWithoutSourceLocation: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger = RecordingSlf4jLogger.allEnabled()
    val canLog = new Log4sLogger(log4s(logger), sourceLocationFormat)
    canLog.debug(message)
    canLog.info(message)
    canLog.warn(message)
    canLog.error(message)
    logger.records ==== Vector(
      Record(Slf4jLevel.DEBUG, message, None),
      Record(Slf4jLevel.INFO, message, None),
      Record(Slf4jLevel.WARN, message, None),
      Record(Slf4jLevel.ERROR, message, None),
    )
  }

  def testWithoutSourceLocationWithThrowable: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger    = RecordingSlf4jLogger.allEnabled()
    val canLog    = new Log4sLogger(log4s(logger), sourceLocationFormat)
    val throwable = new RuntimeException("t")
    canLog.debug(throwable)(message)
    canLog.info(throwable)(message)
    canLog.warn(throwable)(message)
    canLog.error(throwable)(message)
    logger.records ==== Vector(
      Record(Slf4jLevel.DEBUG, message, Some(throwable)),
      Record(Slf4jLevel.INFO, message, Some(throwable)),
      Record(Slf4jLevel.WARN, message, Some(throwable)),
      Record(Slf4jLevel.ERROR, message, Some(throwable)),
    )
  }

  def testWithSourceLocation: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    sourceLocation       <- Gens.genSourceLocation.log("sourceLocation")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger = RecordingSlf4jLogger.allEnabled()
    val canLog = new Log4sLogger(log4s(logger), sourceLocationFormat)
    canLog.debug(sourceLocation)(message)
    canLog.info(sourceLocation)(message)
    canLog.warn(sourceLocation)(message)
    canLog.error(sourceLocation)(message)

    val expected = ExpectedSourceLocationRendering.render(sourceLocationFormat, message, sourceLocation)
    logger.records ==== Vector(
      Record(Slf4jLevel.DEBUG, expected, None),
      Record(Slf4jLevel.INFO, expected, None),
      Record(Slf4jLevel.WARN, expected, None),
      Record(Slf4jLevel.ERROR, expected, None),
    )
  }

  def testWithSourceLocationAndThrowable: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    sourceLocation       <- Gens.genSourceLocation.log("sourceLocation")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger    = RecordingSlf4jLogger.allEnabled()
    val canLog    = new Log4sLogger(log4s(logger), sourceLocationFormat)
    val throwable = new RuntimeException("t")
    canLog.debug(throwable, sourceLocation)(message)
    canLog.info(throwable, sourceLocation)(message)
    canLog.warn(throwable, sourceLocation)(message)
    canLog.error(throwable, sourceLocation)(message)

    val expected = ExpectedSourceLocationRendering.render(sourceLocationFormat, message, sourceLocation)
    logger.records ==== Vector(
      Record(Slf4jLevel.DEBUG, expected, Some(throwable)),
      Record(Slf4jLevel.INFO, expected, Some(throwable)),
      Record(Slf4jLevel.WARN, expected, Some(throwable)),
      Record(Slf4jLevel.ERROR, expected, Some(throwable)),
    )
  }

  def testDefaultSourceLocationFormat: Result = {
    val logger1   = RecordingSlf4jLogger.allEnabled()
    val logger2   = RecordingSlf4jLogger.allEnabled()
    val throwable = new RuntimeException("t")

    new Log4sLogger(log4s(logger1)).info(loc)("i")
    Log4sLogger.log4sCanLogWith(log4s(logger2)).error(throwable, loc)("e")

    Result.all(
      List(
        logger1.records ==== Vector(Record(Slf4jLevel.INFO, "i at com.example.Foo.bar(Foo.scala:42)", None)),
        logger2.records ==== Vector(
          Record(Slf4jLevel.ERROR, "e at com.example.Foo.bar(Foo.scala:42)", Some(throwable))
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
    val logger  = RecordingSlf4jLogger.allDisabled()
    callAllMethods(
      new Log4sLogger(log4s(logger), SourceLocationFormat.beforeMessage("[", "] ")),
      countingMessage(counter),
    )
    Result.all(
      List(
        counter.get() ==== 0,
        logger.records ==== Vector.empty[Record],
      )
    )
  }

  def testEnabledLevelsEvaluateMessageOnce: Result = {
    val counter = new AtomicInteger(0)
    val logger  = RecordingSlf4jLogger.allEnabled()
    callAllMethods(new Log4sLogger(log4s(logger)), countingMessage(counter))
    Result.all(
      List(
        counter.get() ==== 16,
        logger.records.length ==== 16,
      )
    )
  }

  def testOnlyEnabledLevelsLog: Result = {
    val logger = new RecordingSlf4jLogger(Set(Slf4jLevel.INFO, Slf4jLevel.ERROR))
    callAllMethods(new Log4sLogger(log4s(logger)), () => "m")
    logger.records.map(_.level).distinct ==== Vector(Slf4jLevel.INFO, Slf4jLevel.ERROR)
  }

  def testWithoutSourceLocationDropsLocation: Result = {
    val logger    = RecordingSlf4jLogger.allEnabled()
    val canLog    = new Log4sLogger(log4s(logger), SourceLocationFormat.beforeMessage("[", "] ")).withoutSourceLocation
    val throwable = new RuntimeException("t")
    canLog.debug(loc)("d")
    canLog.info(throwable, loc)("i")
    canLog.warn(loc)("w")
    canLog.error(throwable, loc)("e")
    logger.records ==== Vector(
      Record(Slf4jLevel.DEBUG, "d", None),
      Record(Slf4jLevel.INFO, "i", Some(throwable)),
      Record(Slf4jLevel.WARN, "w", None),
      Record(Slf4jLevel.ERROR, "e", Some(throwable)),
    )
  }

  def testFactoriesWithoutSourceLocationFormat: Result = {
    val logger                          = RecordingSlf4jLogger.allEnabled()
    val canLogs: List[(String, CanLog)] = List(
      "new Log4sLogger(log4s(logger))"             -> new Log4sLogger(log4s(logger)),
      "Log4sLogger.log4sCanLogWith(log4s(logger))" -> Log4sLogger.log4sCanLogWith(log4s(logger)),
      "Log4sLogger.log4sCanLog[A]"                 -> Log4sLogger.log4sCanLog[RecordingSlf4jLogger],
      "Log4sLogger.log4sCanLog(name)"              -> Log4sLogger.log4sCanLog("test-logger"),
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
    val logger                          = RecordingSlf4jLogger.allEnabled()
    val canLogs: List[(String, CanLog)] = List(
      "new Log4sLogger(log4s(logger), sourceLocationFormat)"                                          ->
        new Log4sLogger(log4s(logger), sourceLocationFormat),
      "Log4sLogger.log4sCanLogWithSourceLocationFormat[A](sourceLocationFormat)"                      ->
        Log4sLogger.log4sCanLogWithSourceLocationFormat[RecordingSlf4jLogger](sourceLocationFormat),
      "Log4sLogger.log4sCanLogWithSourceLocationFormat(name, sourceLocationFormat)"                   ->
        Log4sLogger.log4sCanLogWithSourceLocationFormat("test-logger", sourceLocationFormat),
      "Log4sLogger.log4sCanLogWithLoggerAndSourceLocationFormat(log4s(logger), sourceLocationFormat)" ->
        Log4sLogger.log4sCanLogWithLoggerAndSourceLocationFormat(log4s(logger), sourceLocationFormat),
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
    val logger = RecordingSlf4jLogger.allEnabled()
    val canLog =
      Log4sLogger.log4sCanLogWithLoggerAndSourceLocationFormat(
        log4s(logger),
        SourceLocationFormat.beforeMessage("[", "] "),
      )
    canLog.info(loc)("i")
    Result.all(
      List(
        canLog match {
          case log4sLogger: Log4sLogger =>
            Result.assert(log4sLogger.logger.logger eq logger).log("Log4sLogger.logger should wrap the given Logger")
          case other =>
            Result.failure.log(s"Expected Log4sLogger but got ${other.getClass.getName}")
        },
        logger.records ==== Vector(Record(Slf4jLevel.INFO, "[com.example.Foo.bar(Foo.scala:42)] i", None)),
      )
    )
  }

}
