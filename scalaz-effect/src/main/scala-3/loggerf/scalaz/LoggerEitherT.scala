package loggerf.scalaz

import scalaz.*
import effectie.scalaz.Fx
import loggerf.logger.CanLog

trait LoggerEitherT[F[_]] {

  given EF: Fx[F]
  given MF: Monad[F]

  given canLog: CanLog

  def debugEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].debugEither(efab.run)(a2String, b2String)
    )

  def infoEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].infoEither(efab.run)(a2String, b2String)
    )

  def warnEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].warnEither(efab.run)(a2String, b2String)
    )

  def errorEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].errorEither(efab.run)(a2String, b2String)
    )
}

object LoggerEitherT {
  def apply[F[_] : LoggerEitherT]: LoggerEitherT[F] = summon[LoggerEitherT[F]]

  given loggerEitherT[F[_]](
    using EF: Fx[F], MF: Monad[F], canLog: CanLog
  ): LoggerEitherT[F] = new LoggerEitherTF[F](EF, MF, canLog)

  final class LoggerEitherTF[F[_]](
      override val EF: Fx[F]
    , override val MF: Monad[F]
    , override val canLog: CanLog
  ) extends LoggerEitherT[F]

}
