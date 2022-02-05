package loggerf.scalaz

import scalaz._
import Scalaz._
import effectie.scalaz.Fx
import loggerf.logger.CanLog

trait LoggerOption[F[_]] {

  implicit val EF: FxCtor[F]
  implicit val MF: Monad[F]

  implicit val logger0: CanLog

  def debugOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.bind(fa) {
      case Some(a) =>
        EF.effectOf(logger0.debug(a2String(a))) *> EF.effectOf(a.some)
      case None =>
        EF.effectOf(logger0.debug(ifEmpty)) *> EF.effectOf(none[A])
    }

  def infoOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.bind(fa) {
      case Some(a) =>
        EF.effectOf(logger0.info(a2String(a))) *> EF.effectOf(a.some)
      case None =>
        EF.effectOf(logger0.info(ifEmpty)) *> EF.effectOf(none[A])
    }

  def warnOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.bind(fa) {
      case Some(a) =>
        EF.effectOf(logger0.warn(a2String(a))) *> EF.effectOf(a.some)
      case None =>
        EF.effectOf(logger0.warn(ifEmpty)) *> EF.effectOf(none[A])
    }

  def errorOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.bind(fa) {
      case Some(a) =>
        EF.effectOf(logger0.error(a2String(a))) *> EF.effectOf(a.some)
      case None =>
        EF.effectOf(logger0.error(ifEmpty)) *> EF.effectOf(none[A])
    }
}

object LoggerOption {
  def apply[F[_]: LoggerOption]: LoggerOption[F] = implicitly[LoggerOption[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerOption[F[_]](
    implicit EF: FxCtor[F],
    MF: Monad[F],
    canLog: CanLog,
  ): LoggerOption[F] = new LoggerOptionF[F]

  final class LoggerOptionF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: FxCtor[F],
    override val MF: Monad[F],
    override val logger0: CanLog,
  ) extends LoggerOption[F]

}
