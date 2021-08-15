package loggerf.cats

import cats.Monad
import effectie.cats.FxCtor
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

  def apply[F[_]: Loggers]: Loggers[F] = implicitly[Loggers[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggers[F[_]](implicit EF: FxCtor[F], MF: Monad[F], canLog: CanLog): Loggers[F] =
    new LoggersF[F]

  final class LoggersF[F[_]: FxCtor: Monad](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: FxCtor[F],
    override val MF: Monad[F],
    override val canLog: CanLog,
  ) extends Loggers[F]
      with LoggerA[F]
      with LoggerOption[F]
      with LoggerEither[F]
      with LoggerOptionT[F]
      with LoggerEitherT[F]
      with Log[F]

}
