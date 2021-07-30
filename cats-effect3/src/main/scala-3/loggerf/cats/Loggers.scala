package loggerf.cats

import cats.Monad
import effectie.cats.Fx
import loggerf.logger.CanLog

/** @author Kevin Lee
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

  def apply[F[_]: Loggers]: Loggers[F] = summon[Loggers[F]]

  given loggers[F[_]](using EF: Fx[F], MF: Monad[F], logger: CanLog): Loggers[F] =
    new LoggersF[F]

  final class LoggersF[F[_]: Fx: Monad](
    using override val EF0: Fx[F],
    override val MF0: Monad[F],
    override val canLog: CanLog
  ) extends Loggers[F]
      with LoggerA[F]
      with LoggerOption[F]
      with LoggerEither[F]
      with LoggerOptionT[F]
      with LoggerEitherT[F]
      with Log[F]

}
