package loggerf.monix

import cats.*
import cats.data.EitherT
import effectie.core.FxCtor
import loggerf.logger.CanLog

trait LoggerEitherT[F[_]] {

  given EF: FxCtor[F]
  given MF: Monad[F]

  given canLog: CanLog

  def debugEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String,
    b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].debugEither(efab.value)(a2String, b2String)
    )

  def infoEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String,
    b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].infoEither(efab.value)(a2String, b2String)
    )

  def warnEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String,
    b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].warnEither(efab.value)(a2String, b2String)
    )

  def errorEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String,
    b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].errorEither(efab.value)(a2String, b2String)
    )
}

object LoggerEitherT {
  def apply[F[_]: LoggerEitherT]: LoggerEitherT[F] = summon[LoggerEitherT[F]]

  given loggerEitherT[F[_]](
    using EF: FxCtor[F],
    MF: Monad[F],
    canLog: CanLog
  ): LoggerEitherT[F] = new LoggerEitherTF[F](EF, MF, canLog)

  final class LoggerEitherTF[F[_]](
    override val EF: FxCtor[F],
    override val MF: Monad[F],
    override val canLog: CanLog
  ) extends LoggerEitherT[F]

}
