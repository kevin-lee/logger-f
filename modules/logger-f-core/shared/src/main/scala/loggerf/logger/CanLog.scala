package loggerf.logger

import loggerf.{Level, SourceLocation}

import scala.annotation.implicitNotFound

/** @author Kevin Lee
  * @since 2020-03-28
  */
@implicitNotFound(
  """
  Could not find an implicit CanLog.
  You can probably find it from the loggerf.logger package.
  ---
  If you use slf4j or logback, get logger-f-slf4j then,

    import loggerf.logger._

    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[MyAppType]
    // or
    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[this.type]

    // or
    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("my-logger-name")

    // or
    implicit val canLog: CanLog = Slf4JLogger.slf4JCanLogWith(org.slf4j.LoggerFactory.getLogger(getClass))

  ---
  If you use log4s, get logger-f-log4s then,

    import loggerf.logger._
    implicit val canLog: CanLog = Log4sLogger.log4sCanLog[MyAppType]
    // or
    implicit val canLog: CanLog = Log4sLogger.log4sCanLog[this.type]

    // or
    implicit val canLog: CanLog = Log4sLogger.log4sCanLog("my-logger-name")

    // or
    implicit val canLog: CanLog = Log4sLogger.log4sCanLogWith(org.log4s.getLogger)
  ---

  ---
  If you use log4j, get logger-f-log4j then,

    import loggerf.logger._
    implicit val canLog: CanLog = Log4jLogger.log4jCanLog[MyAppType]
    // or
    implicit val canLog: CanLog = Log4jLogger.log4jCanLog[this.type]

    // or
    implicit val canLog: CanLog = Log4jLogger.log4jCanLog("my-logger-name")

    // or
    implicit val canLog: CanLog = Log4jLogger.log4jCanLogWith(org.apache.logging.log4j.LogManager.getLogger(getClass))
  ---
  """
)
trait CanLog {

  protected def sourceLocationPosition: CanLog.SourceLocationPosition

  protected def sourceLocationPrefix: String
  protected def sourceLocationSuffix: String

  private lazy val renderWithSourceLocation: (String, SourceLocation) => String = {

    lazy val renderFunction: (String, String) => (String, SourceLocation) => String =
      sourceLocationPosition match {
        case CanLog.SourceLocationPosition.AfterMessage =>
          CanLog.renderWithSourceLocationWithPrefixAndSuffixAfterMessage

        case CanLog.SourceLocationPosition.BeforeMessage =>
          CanLog.renderWithSourceLocationWithPrefixAndSuffixBeforeMessage
      }

    (sourceLocationPrefix, sourceLocationSuffix) match {
      case (prefix, suffix) if prefix.nonEmpty && suffix.nonEmpty =>
        renderFunction(prefix, suffix)(_, _)

      case (prefix, _) if prefix.nonEmpty =>
        renderFunction(prefix, "")(_, _)

      case (_, suffix) if suffix.nonEmpty =>
        renderFunction("", suffix)(_, _)

      case (_, _) =>
        CanLog.defaultRenderWithSourceLocation
    }
  }

  protected def newCanLogWithNewSourceLocationConfig(
    sourceLocationPosition: CanLog.SourceLocationPosition,
    sourceLocationPrefix: String,
    sourceLocationSuffix: String,
  ): CanLog

  def debug(message: => String): Unit
  def debug(throwable: Throwable)(message: => String): Unit
  def info(message: => String): Unit
  def info(throwable: Throwable)(message: => String): Unit
  def warn(message: => String): Unit
  def warn(throwable: Throwable)(message: => String): Unit
  def error(message: => String): Unit
  def error(throwable: Throwable)(message: => String): Unit

  /* The location-aware variants. By default the location is rendered into the message, so every existing
   * backend gets it for free. A backend may override them to pass the location structurally instead.
   * The argument passed to the by-name parameter is itself an unevaluated expression, so the message is never
   * built when the backend has the level disabled.
   */
  def debug(sourceLocation: SourceLocation)(message: => String): Unit =
    debug(renderWithSourceLocation(message, sourceLocation))

  def debug(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    debug(throwable)(renderWithSourceLocation(message, sourceLocation))

  def info(sourceLocation: SourceLocation)(message: => String): Unit =
    info(renderWithSourceLocation(message, sourceLocation))

  def info(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    info(throwable)(renderWithSourceLocation(message, sourceLocation))

  def warn(sourceLocation: SourceLocation)(message: => String): Unit =
    warn(renderWithSourceLocation(message, sourceLocation))

  def warn(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    warn(throwable)(renderWithSourceLocation(message, sourceLocation))

  def error(sourceLocation: SourceLocation)(message: => String): Unit =
    error(renderWithSourceLocation(message, sourceLocation))

  def error(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
    error(throwable)(renderWithSourceLocation(message, sourceLocation))
}

object CanLog {

  sealed trait SourceLocationPosition
  object SourceLocationPosition {
    case object BeforeMessage extends SourceLocationPosition
    case object AfterMessage extends SourceLocationPosition

    def beforeMessage: SourceLocationPosition = BeforeMessage
    def afterMessage: SourceLocationPosition  = AfterMessage
  }

  def renderWithSourceLocationWithPrefixAndSuffixBeforeMessage(
    sourceLocationPrefix: String,
    sourceLocationSuffix: String,
  )(
    message: String,
    sourceLocation: SourceLocation,
  ): String =
    s"$sourceLocationPrefix${sourceLocation.render}$sourceLocationSuffix$message"

  def renderWithSourceLocationWithPrefixAndSuffixAfterMessage(
    sourceLocationPrefix: String,
    sourceLocationSuffix: String,
  )(
    message: String,
    sourceLocation: SourceLocation,
  ): String =
    s"$message$sourceLocationPrefix${sourceLocation.render}$sourceLocationSuffix"

  def defaultRenderWithSourceLocation(message: String, sourceLocation: SourceLocation): String =
    s"$message at ${sourceLocation.render}"

  implicit class GetLogger(private val canLog: CanLog) extends AnyVal {
    @inline def getLogger(level: Level): (=> String) => Unit = level match {
      case Level.Debug => message => canLog.debug(message)
      case Level.Info => message => canLog.info(message)
      case Level.Warn => message => canLog.warn(message)
      case Level.Error => message => canLog.error(message)
    }

    @inline def getLoggerWithSourceLocation(level: Level): SourceLocation => (=> String) => Unit = level match {
      case Level.Debug => sourceLocation => message => canLog.debug(sourceLocation)(message)
      case Level.Info => sourceLocation => message => canLog.info(sourceLocation)(message)
      case Level.Warn => sourceLocation => message => canLog.warn(sourceLocation)(message)
      case Level.Error => sourceLocation => message => canLog.error(sourceLocation)(message)
    }

    @inline def getLoggerWithThrowableAndSourceLocation(
      level: Level
    ): Throwable => SourceLocation => (=> String) => Unit = level match {
      case Level.Debug => throwable => sourceLocation => message => canLog.debug(throwable, sourceLocation)(message)
      case Level.Info => throwable => sourceLocation => message => canLog.info(throwable, sourceLocation)(message)
      case Level.Warn => throwable => sourceLocation => message => canLog.warn(throwable, sourceLocation)(message)
      case Level.Error => throwable => sourceLocation => message => canLog.error(throwable, sourceLocation)(message)
    }
  }

  implicit class CanLogOps(private val canLog: CanLog) extends AnyVal {

    /** A `CanLog` which logs exactly what `canLog` logs, minus the source location. */
    def withoutSourceLocation: CanLog = new CanLog.WithoutSourceLocation(canLog)

    def withSourceLocationConfig(
      sourceLocationPosition: SourceLocationPosition = canLog.sourceLocationPosition,
      sourceLocationPrefix: String = canLog.sourceLocationPrefix,
      sourceLocationSuffix: String = canLog.sourceLocationSuffix,
    ): CanLog =
      canLog.newCanLogWithNewSourceLocationConfig(
        sourceLocationPosition,
        sourceLocationPrefix,
        sourceLocationSuffix,
      )
  }

  final class WithoutSourceLocation(val underlying: CanLog) extends CanLog {

    @inline
    override def sourceLocationPosition: SourceLocationPosition = underlying.sourceLocationPosition

    @inline
    override def sourceLocationPrefix: String = underlying.sourceLocationPrefix

    @inline
    override def sourceLocationSuffix: String = underlying.sourceLocationSuffix

    override def newCanLogWithNewSourceLocationConfig(
      sourceLocationPosition: SourceLocationPosition,
      sourceLocationPrefix: String,
      sourceLocationSuffix: String,
    ): CanLog =
      this

    override def debug(message: => String): Unit = underlying.debug(message)

    override def debug(throwable: Throwable)(message: => String): Unit = underlying.debug(throwable)(message)

    override def info(message: => String): Unit = underlying.info(message)

    override def info(throwable: Throwable)(message: => String): Unit = underlying.info(throwable)(message)

    override def warn(message: => String): Unit = underlying.warn(message)

    override def warn(throwable: Throwable)(message: => String): Unit = underlying.warn(throwable)(message)

    override def error(message: => String): Unit = underlying.error(message)

    override def error(throwable: Throwable)(message: => String): Unit = underlying.error(throwable)(message)

    override def debug(sourceLocation: SourceLocation)(message: => String): Unit = underlying.debug(message)

    override def debug(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      underlying.debug(throwable)(message)

    override def info(sourceLocation: SourceLocation)(message: => String): Unit = underlying.info(message)

    override def info(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      underlying.info(throwable)(message)

    override def warn(sourceLocation: SourceLocation)(message: => String): Unit = underlying.warn(message)

    override def warn(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      underlying.warn(throwable)(message)

    override def error(sourceLocation: SourceLocation)(message: => String): Unit = underlying.error(message)

    override def error(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      underlying.error(throwable)(message)
  }
}
