package loggerf.cats

import cats.Monad
import effectie.cats.Fx
import loggerf.logger.CanLog

/**
 * @author Kevin Lee
 * @since 2020-03-25
 */
trait Loggers[F[_]]
  extends LoggerA[F]
  with LoggerOption[F]
  with LoggerEither[F]
  with LoggerOptionT[F]
  with LoggerEitherT[F]
  with Log[F]

object Loggers {

  def apply[F[_] : Loggers]: Loggers[F] = implicitly[Loggers[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggers[F[_]](implicit EF: Fx[F], MF: Monad[F], canLog: CanLog): Loggers[F] =
    new LoggersF[F]

  final class LoggersF[F[_] : Fx : Monad](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: Fx[F]
  , override val MF: Monad[F]
  , override val canLog: CanLog
  ) extends Loggers[F]
    with LoggerA[F]
    with LoggerOption[F]
    with LoggerEither[F]
    with LoggerOptionT[F]
    with LoggerEitherT[F]
    with Log[F]

}
