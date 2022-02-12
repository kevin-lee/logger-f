package loggerf.cats

import cats.Monad
import effectie.core.FxCtor
import loggerf.core.Log
import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2020-04-10
  */
trait instances {

  given logF[F[*]](
    using EF: FxCtor[F],
    canLog: CanLog,
    MF: Monad[F]
  ): Log[F] =
    new LogF[F](EF, canLog, MF)

  final class LogF[F[*]](
    override val EF: FxCtor[F],
    override val canLog: CanLog,
    val MF: Monad[F],
  ) extends Log[F] {
    override def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B] = MF.flatMap(fa)(f)
  }

}

object instances extends instances
