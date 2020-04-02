package loggerf

import cats._
import cats.implicits._

import effectie.cats.EffectConstructor

trait LoggerOption[F[_]] {

  implicit val FE0: EffectConstructor[F]
  implicit val FM0: Monad[F]

  implicit val logger0: Logger

  def debugOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    FM0.flatMap(fa) {
      case Some(a) =>
        FE0.effectOf(logger0.debug(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.debug(ifEmpty)) *> FE0.effectOf(none[A])
    }

  def infoOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    FM0.flatMap(fa) {
      case Some(a) =>
        FE0.effectOf(logger0.info(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.info(ifEmpty)) *> FE0.effectOf(none[A])
    }

  def warnOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    FM0.flatMap(fa) {
      case Some(a) =>
        FE0.effectOf(logger0.warn(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.warn(ifEmpty)) *> FE0.effectOf(none[A])
    }

  def errorOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    FM0.flatMap(fa) {
      case Some(a) =>
        FE0.effectOf(logger0.error(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.error(ifEmpty)) *> FE0.effectOf(none[A])
    }
}

object LoggerOption {
  def apply[F[_] : LoggerOption]: LoggerOption[F] = implicitly[LoggerOption[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerOption[F[_]](
    implicit FE: EffectConstructor[F], FM: Monad[F], logger: Logger
  ): LoggerOption[F] = new LoggerOptionF[F]

  final class LoggerOptionF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val FE0: EffectConstructor[F]
  , override val FM0: Monad[F]
  , override val logger0: Logger
  ) extends LoggerOption[F]

}