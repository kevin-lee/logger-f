package loggerf.scalaz

import effectie.scalaz.Fx
import loggerf.logger.CanLog
import scalaz.Monad

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

  def apply[F[_] : Loggers]: Loggers[F] = summon[Loggers[F]]

  given loggers[F[_]](using EF: FxCtor[F], MF: Monad[F], canLog: CanLog): Loggers[F] =
    new LoggersF[F](EF, MF, canLog)

  final class LoggersF[F[_] : FxCtor : Monad](
    override val EF: FxCtor[F]
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
