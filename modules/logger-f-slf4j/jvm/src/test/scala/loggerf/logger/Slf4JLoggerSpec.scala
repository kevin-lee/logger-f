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
object Slf4JLoggerSpec extends Properties {

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
      "slf4JCanLogWithLoggerAndSourceLocationFormat logs with the given Logger",
      testWithLoggerAndSourceLocationFormatUsesGivenLogger,
    ),
  )

  private val loc: SourceLocation = SourceLocation("com.example.Foo", "bar", "Foo.scala", 42)

  def testWithoutSourceLocation: Property = for {
    sourceLocationFormat <- Gens.genSourceLocationFormat.log("sourceLocationFormat")
    message              <- Gens.genMessage.log("message")
  } yield {
    val logger = RecordingSlf4jLogger.allEnabled()
    val canLog = new Slf4JLogger(logger, sourceLocationFormat)
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
    val canLog    = new Slf4JLogger(logger, sourceLocationFormat)
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
    val canLog = new Slf4JLogger(logger, sourceLocationFormat)
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
    val canLog    = new Slf4JLogger(logger, sourceLocationFormat)
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

    new Slf4JLogger(logger1).info(loc)("i")
    Slf4JLogger.slf4JCanLogWith(logger2).error(throwable, loc)("e")

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
    callAllMethods(new Slf4JLogger(logger, SourceLocationFormat.beforeMessage("[", "] ")), countingMessage(counter))
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
    callAllMethods(new Slf4JLogger(logger), countingMessage(counter))
    Result.all(
      List(
        counter.get() ==== 16,
        logger.records.length ==== 16,
      )
    )
  }

  def testOnlyEnabledLevelsLog: Result = {
    val logger = new RecordingSlf4jLogger(Set(Slf4jLevel.INFO, Slf4jLevel.ERROR))
    callAllMethods(new Slf4JLogger(logger), () => "m")
    logger.records.map(_.level).distinct ==== Vector(Slf4jLevel.INFO, Slf4jLevel.ERROR)
  }

  def testWithoutSourceLocationDropsLocation: Result = {
    val logger    = RecordingSlf4jLogger.allEnabled()
    val canLog    = new Slf4JLogger(logger, SourceLocationFormat.beforeMessage("[", "] ")).withoutSourceLocation
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
      "new Slf4JLogger(logger)"             -> new Slf4JLogger(logger),
      "Slf4JLogger.slf4JCanLogWith(logger)" -> Slf4JLogger.slf4JCanLogWith(logger),
      "Slf4JLogger.slf4JCanLog[A]"          -> Slf4JLogger.slf4JCanLog[RecordingSlf4jLogger],
      "Slf4JLogger.slf4JCanLog(name)"       -> Slf4JLogger.slf4JCanLog("test-logger"),
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
      "new Slf4JLogger(logger, sourceLocationFormat)"                                          ->
        new Slf4JLogger(logger, sourceLocationFormat),
      "Slf4JLogger.slf4JCanLogWithSourceLocationFormat[A](sourceLocationFormat)"               ->
        Slf4JLogger.slf4JCanLogWithSourceLocationFormat[RecordingSlf4jLogger](sourceLocationFormat),
      "Slf4JLogger.slf4JCanLogWithSourceLocationFormat(name, sourceLocationFormat)"            ->
        Slf4JLogger.slf4JCanLogWithSourceLocationFormat("test-logger", sourceLocationFormat),
      "Slf4JLogger.slf4JCanLogWithLoggerAndSourceLocationFormat(logger, sourceLocationFormat)" ->
        Slf4JLogger.slf4JCanLogWithLoggerAndSourceLocationFormat(logger, sourceLocationFormat),
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
      Slf4JLogger.slf4JCanLogWithLoggerAndSourceLocationFormat(logger, SourceLocationFormat.beforeMessage("[", "] "))
    canLog.info(loc)("i")
    Result.all(
      List(
        canLog match {
          case slf4JLogger: Slf4JLogger =>
            Result.assert(slf4JLogger.logger eq logger).log("Slf4JLogger.logger should be the given Logger")
          case other =>
            Result.failure.log(s"Expected Slf4JLogger but got ${other.getClass.getName}")
        },
        logger.records ==== Vector(Record(Slf4jLevel.INFO, "[com.example.Foo.bar(Foo.scala:42)] i", None)),
      )
    )
  }

}
