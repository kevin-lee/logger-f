package loggerf.cats

import cats.*
import cats.data.EitherT
import effectie.cats.Fx
import loggerf.logger.CanLog

trait LoggerEitherT[F[_]] {

  given EF0: Fx[F]
  given MF0: Monad[F]

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
    using EF: Fx[F],
    MF: Monad[F],
    logger: CanLog
  ): LoggerEitherT[F] = new LoggerEitherTF[F]

  final class LoggerEitherTF[F[_]](
    using override val EF0: Fx[F],
    override val MF0: Monad[F],
    override val canLog: CanLog
  ) extends LoggerEitherT[F]

}
