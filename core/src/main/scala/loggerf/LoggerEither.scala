package loggerf

import cats._
import cats.implicits._

import just.effect.EffectConstructor

trait LoggerEither[F[_]] {

  implicit val FE0: EffectConstructor[F]
  implicit val FM0: Monad[F]

  implicit val logger0: Logger

  def debugEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    FM0.flatMap(fab) {
      case Left(a) =>
        FE0.effect(logger0.debug(a2String(a))) *> FE0.effect(a.asLeft[B])
      case Right(b) =>
        FE0.effect(logger0.debug(b2String(b))) *> FE0.effect(b.asRight[A])
    }

  def infoEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    FM0.flatMap(fab) {
      case Left(a) =>
        FE0.effect(logger0.info(a2String(a))) *> FE0.effect(a.asLeft[B])
      case Right(b) =>
        FE0.effect(logger0.info(b2String(b))) *> FE0.effect(b.asRight[A])
    }

  def warnEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    FM0.flatMap(fab) {
      case Left(a) =>
        FE0.effect(logger0.warn(a2String(a))) *> FE0.effect(a.asLeft[B])
      case Right(b) =>
        FE0.effect(logger0.warn(b2String(b))) *> FE0.effect(b.asRight[A])
    }

  def errorEither[A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    FM0.flatMap(fab) {
      case Left(a) =>
        FE0.effect(logger0.error(a2String(a))) *> FE0.effect(a.asLeft[B])
      case Right(b) =>
        FE0.effect(logger0.error(b2String(b))) *> FE0.effect(b.asRight[A])
    }
}

object LoggerEither {
  def apply[F[_] : LoggerEither]: LoggerEither[F] = implicitly[LoggerEither[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerEither[F[_]](
    implicit FE: EffectConstructor[F], FM: Monad[F], logger: Logger
  ): LoggerEither[F] = new LoggerEitherF[F]

  final class LoggerEitherF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val FE0: EffectConstructor[F]
  , override val FM0: Monad[F]
  , override val logger0: Logger
  ) extends LoggerEither[F]

}