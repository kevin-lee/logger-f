package loggerf.cats

import cats.Monad
import effectie.core.FxCtor
import loggerf.core.Log
import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2020-04-10
  */
object instances {

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def logF[F[_]](
    implicit EF: FxCtor[F],
    MF: Monad[F],
    canLog: CanLog,
  ): Log[F] =
    new LogF[F](EF, canLog, MF)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  final class LogF[F[_]](
    override val EF: FxCtor[F],
    override val canLog: CanLog,
    val MF: Monad[F],
  ) extends Log[F] {
    @inline override def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B] = MF.flatMap(fa)(f)
  }

}
