package loggerf.logger

import org.log4s.Logger

import scala.reflect.ClassTag

/** @author Kevin Lee
  * @since 2020-09-12
  */
trait Log4sLogger extends CanLog
object Log4sLogger {

  private final case class DefaultLog4sLogger(
    logger: Logger,
    override protected val sourceLocationPosition: CanLog.SourceLocationPosition,
    override protected val sourceLocationPrefix: String,
    override protected val sourceLocationSuffix: String,
  ) extends Log4sLogger {

    override protected def newCanLogWithNewSourceLocationConfig(
      sourceLocationPosition: CanLog.SourceLocationPosition,
      sourceLocationPrefix: String,
      sourceLocationSuffix: String,
    ): CanLog =
      copy(
        logger = logger,
        sourceLocationPosition = sourceLocationPosition,
        sourceLocationPrefix = sourceLocationPrefix,
        sourceLocationSuffix = sourceLocationSuffix,
      )

    override def debug(message: => String): Unit = logger.debug(message)

    override def debug(throwable: Throwable)(message: => String): Unit = logger.debug(throwable)(message)

    override def info(message: => String): Unit = logger.info(message)

    override def info(throwable: Throwable)(message: => String): Unit = logger.info(throwable)(message)

    override def warn(message: => String): Unit = logger.warn(message)

    override def warn(throwable: Throwable)(message: => String): Unit = logger.warn(throwable)(message)

    override def error(message: => String): Unit = logger.error(message)

    override def error(throwable: Throwable)(message: => String): Unit = logger.error(throwable)(message)
  }

  def log4sCanLog[A](implicit aClass: ClassTag[A]): CanLog =
    DefaultLog4sLogger(
      org.log4s.getLogger(aClass.runtimeClass),
      CanLog.SourceLocationPosition.AfterMessage,
      "",
      "",
    )

  def log4sCanLog(name: String): CanLog =
    DefaultLog4sLogger(org.log4s.getLogger(name), CanLog.SourceLocationPosition.AfterMessage, "", "")

  def log4sCanLogWith(logger: Logger): CanLog =
    DefaultLog4sLogger(logger, CanLog.SourceLocationPosition.AfterMessage, "", "")

}
