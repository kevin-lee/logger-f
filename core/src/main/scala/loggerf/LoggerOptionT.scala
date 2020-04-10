package loggerf

import cats._
import cats.data.OptionT
import cats.implicits._
import effectie.cats.EffectConstructor

trait LoggerOptionT[F[_]] {

  implicit val FE0: EffectConstructor[F]
  implicit val FM0: Monad[F]

  implicit val logger0: Logger

  def debugOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      FM0.flatMap(ofa.value) {
      case Some(a) =>
        FE0.effectOf(logger0.debug(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.debug(ifEmpty)) *> FE0.effectOf(none[A])
      }
    )

  def infoOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      FM0.flatMap(ofa.value) {
      case Some(a) =>
        FE0.effectOf(logger0.info(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.info(ifEmpty)) *> FE0.effectOf(none[A])
      }
    )

  def warnOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      FM0.flatMap(ofa.value) {
      case Some(a) =>
        FE0.effectOf(logger0.warn(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.warn(ifEmpty)) *> FE0.effectOf(none[A])
      }
    )

  def errorOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      FM0.flatMap(ofa.value) {
      case Some(a) =>
        FE0.effectOf(logger0.error(a2String(a))) *> FE0.effectOf(a.some)
      case None =>
        FE0.effectOf(logger0.error(ifEmpty)) *> FE0.effectOf(none[A])
      }
    )

}

object LoggerOptionT {
  def apply[F[_] : LoggerOptionT]: LoggerOptionT[F] = implicitly[LoggerOptionT[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerOptionT[F[_]](
    implicit FE: EffectConstructor[F], FM: Monad[F], logger: Logger
  ): LoggerOptionT[F] = new LoggerOptionTF[F]

  final class LoggerOptionTF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val FE0: EffectConstructor[F]
  , override val FM0: Monad[F]
  , override val logger0: Logger
  ) extends LoggerOptionT[F]

}
