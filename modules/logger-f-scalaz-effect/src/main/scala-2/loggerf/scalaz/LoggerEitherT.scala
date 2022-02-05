package loggerf.scalaz

import scalaz._
import effectie.scalaz.Fx
import loggerf.logger.CanLog

trait LoggerEitherT[F[_]] {

  implicit val EF: Fx[F]
  implicit val MF: Monad[F]

  implicit val logger0: CanLog

  def debugEitherT[A, B](
    efab: EitherT[F, A, B],
  )(
    a2String: A => String,
    b2String: B => String,
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].debugEither(efab.run)(a2String, b2String),
    )

  def infoEitherT[A, B](
    efab: EitherT[F, A, B],
  )(
    a2String: A => String,
    b2String: B => String,
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].infoEither(efab.run)(a2String, b2String),
    )

  def warnEitherT[A, B](
    efab: EitherT[F, A, B],
  )(
    a2String: A => String,
    b2String: B => String,
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].warnEither(efab.run)(a2String, b2String),
    )

  def errorEitherT[A, B](
    efab: EitherT[F, A, B],
  )(
    a2String: A => String,
    b2String: B => String,
  ): EitherT[F, A, B] =
    EitherT(
      LoggerEither[F].errorEither(efab.run)(a2String, b2String),
    )
}

object LoggerEitherT {
  def apply[F[_]: LoggerEitherT]: LoggerEitherT[F] = implicitly[LoggerEitherT[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerEitherT[F[_]](
    implicit EF: Fx[F],
    MF: Monad[F],
    canLog: CanLog,
  ): LoggerEitherT[F] = new LoggerEitherTF[F]

  final class LoggerEitherTF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: Fx[F],
    override val MF: Monad[F],
    override val logger0: CanLog,
  ) extends LoggerEitherT[F]

}
