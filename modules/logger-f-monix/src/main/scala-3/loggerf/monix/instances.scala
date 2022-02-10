package loggerf.monix

import cats.*
import cats.data.{EitherT, OptionT}
import cats.syntax.all.*
import effectie.core.FxCtor
import effectie.syntax.all.*
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, NotIgnorable}
import loggerf.core.Log
import loggerf.logger.CanLog
import loggerf.syntax.*

/** @author Kevin Lee
  * @since 2020-04-10
  */
object instances {

  given logF[F[*]](
    using EF: FxCtor[F],
    canLog: CanLog,
    MF: Monad[F]
  ): Log[F] =
    new LogF[F](EF, canLog, MF)

  final class LogF[F[*]](
    override val EF: FxCtor[F],
    override val canLog: CanLog,
    val MF: Monad[F]
  ) extends Log[F] {
    override def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B] = MF.flatMap(fa)(f)
  }

}
