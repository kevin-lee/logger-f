package loggerf.cats

import cats._
import cats.implicits._
import effectie.cats.EffectConstructor
import loggerf.logger.CanLog

trait LoggerEither[F[_]] {

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  implicit val canLog: CanLog

  def debugEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF0.flatMap(fab) {
      case Left(a) =>
        EF0.effectOf(canLog.debug(a2String(a))) *> EF0.effectOf(a.asLeft[B])
      case Right(b) =>
        EF0.effectOf(canLog.debug(b2String(b))) *> EF0.effectOf(b.asRight[A])
    }

  def infoEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF0.flatMap(fab) {
      case Left(a) =>
        EF0.effectOf(canLog.info(a2String(a))) *> EF0.effectOf(a.asLeft[B])
      case Right(b) =>
        EF0.effectOf(canLog.info(b2String(b))) *> EF0.effectOf(b.asRight[A])
    }

  def warnEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF0.flatMap(fab) {
      case Left(a) =>
        EF0.effectOf(canLog.warn(a2String(a))) *> EF0.effectOf(a.asLeft[B])
      case Right(b) =>
        EF0.effectOf(canLog.warn(b2String(b))) *> EF0.effectOf(b.asRight[A])
    }

  def errorEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    MF0.flatMap(fab) {
      case Left(a) =>
        EF0.effectOf(canLog.error(a2String(a))) *> EF0.effectOf(a.asLeft[B])
      case Right(b) =>
        EF0.effectOf(canLog.error(b2String(b))) *> EF0.effectOf(b.asRight[A])
    }
}

object LoggerEither {
  def apply[F[_] : LoggerEither]: LoggerEither[F] = implicitly[LoggerEither[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerEither[F[_]](
    implicit EF: EffectConstructor[F], MF: Monad[F], logger: CanLog
  ): LoggerEither[F] = new LoggerEitherF[F]

  final class LoggerEitherF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF0: EffectConstructor[F]
  , override val MF0: Monad[F]
  , override val canLog: CanLog
  ) extends LoggerEither[F]

}