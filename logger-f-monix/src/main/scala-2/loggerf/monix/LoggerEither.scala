package loggerf.monix

import cats._
import cats.implicits._
import effectie.monix.Fx
import loggerf.logger.CanLog

trait LoggerEither[F[_]] {

  implicit val EF: Fx[F]
  implicit val MF: Monad[F]

  def canLog: CanLog

  def debugEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF.flatMap(fab) {
      case Left(a) =>
        EF.effectOf(canLog.debug(a2String(a))) *> EF.effectOf(a.asLeft[B])
      case Right(b) =>
        EF.effectOf(canLog.debug(b2String(b))) *> EF.effectOf(b.asRight[A])
    }

  def infoEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF.flatMap(fab) {
      case Left(a) =>
        EF.effectOf(canLog.info(a2String(a))) *> EF.effectOf(a.asLeft[B])
      case Right(b) =>
        EF.effectOf(canLog.info(b2String(b))) *> EF.effectOf(b.asRight[A])
    }

  def warnEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF.flatMap(fab) {
      case Left(a) =>
        EF.effectOf(canLog.warn(a2String(a))) *> EF.effectOf(a.asLeft[B])
      case Right(b) =>
        EF.effectOf(canLog.warn(b2String(b))) *> EF.effectOf(b.asRight[A])
    }

  def errorEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF.flatMap(fab) {
      case Left(a) =>
        EF.effectOf(canLog.error(a2String(a))) *> EF.effectOf(a.asLeft[B])
      case Right(b) =>
        EF.effectOf(canLog.error(b2String(b))) *> EF.effectOf(b.asRight[A])
    }
}

object LoggerEither {
  def apply[F[_]: LoggerEither]: LoggerEither[F] = implicitly[LoggerEither[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerEither[F[_]](
    implicit EF: Fx[F],
    MF: Monad[F],
    canLog: CanLog,
  ): LoggerEither[F] = new LoggerEitherF[F]

  final class LoggerEitherF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: Fx[F],
    override val MF: Monad[F],
    override val canLog: CanLog,
  ) extends LoggerEither[F]

}
