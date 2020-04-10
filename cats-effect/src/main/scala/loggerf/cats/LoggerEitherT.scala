package loggerf.cats

import cats._
import cats.data.EitherT
import cats.implicits._

import effectie.cats.EffectConstructor

import loggerf.Logger

trait LoggerEitherT[F[_]] {

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  implicit val logger0: Logger

  def debugEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      MF0.flatMap(efab.value) {
        case Left(a) =>
          EF0.effectOf(logger0.debug(a2String(a))) *> EF0.effectOf(a.asLeft[B])
        case Right(b) =>
          EF0.effectOf(logger0.debug(b2String(b))) *> EF0.effectOf(b.asRight[A])
      }
    )

  def infoEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      MF0.flatMap(efab.value) {
        case Left(a) =>
          EF0.effectOf(logger0.info(a2String(a))) *> EF0.effectOf(a.asLeft[B])
        case Right(b) =>
          EF0.effectOf(logger0.info(b2String(b))) *> EF0.effectOf(b.asRight[A])
      }
    )

  def warnEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      MF0.flatMap(efab.value) {
        case Left(a) =>
          EF0.effectOf(logger0.warn(a2String(a))) *> EF0.effectOf(a.asLeft[B])
        case Right(b) =>
          EF0.effectOf(logger0.warn(b2String(b))) *> EF0.effectOf(b.asRight[A])
      }
    )

  def errorEitherT[A, B](
    efab: EitherT[F, A, B]
  )(
    a2String: A => String
  , b2String: B => String
  ): EitherT[F, A, B] =
    EitherT(
      MF0.flatMap(efab.value) {
        case Left(a) =>
          EF0.effectOf(logger0.error(a2String(a))) *> EF0.effectOf(a.asLeft[B])
        case Right(b) =>
          EF0.effectOf(logger0.error(b2String(b))) *> EF0.effectOf(b.asRight[A])
      }
    )
}

object LoggerEitherT {
  def apply[F[_] : LoggerEitherT]: LoggerEitherT[F] = implicitly[LoggerEitherT[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerEitherT[F[_]](
    implicit EF: EffectConstructor[F], MF: Monad[F], logger: Logger
  ): LoggerEitherT[F] = new LoggerEitherTF[F]

  final class LoggerEitherTF[F[_]](
      @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
      implicit override val EF0: EffectConstructor[F]
    , override val MF0: Monad[F]
    , override val logger0: Logger
  ) extends LoggerEitherT[F]

}
