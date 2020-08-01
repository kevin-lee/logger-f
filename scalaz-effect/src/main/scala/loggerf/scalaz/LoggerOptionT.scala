package loggerf.scalaz

import scalaz._
import effectie.scalaz.EffectConstructor
import loggerf.logger.Logger

trait LoggerOptionT[F[_]] {

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  implicit val logger0: Logger

  def debugOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].debugOption(ofa.run)(ifEmpty, a2String)
    )

  def infoOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].infoOption(ofa.run)(ifEmpty, a2String)
    )

  def warnOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].warnOption(ofa.run)(ifEmpty, a2String)
    )

  def errorOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].errorOption(ofa.run)(ifEmpty, a2String)
    )

}

object LoggerOptionT {
  def apply[F[_] : LoggerOptionT]: LoggerOptionT[F] = implicitly[LoggerOptionT[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerOptionT[F[_]](
    implicit EF: EffectConstructor[F], MF: Monad[F], logger: Logger
  ): LoggerOptionT[F] = new LoggerOptionTF[F]

  final class LoggerOptionTF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF0: EffectConstructor[F]
  , override val MF0: Monad[F]
  , override val logger0: Logger
  ) extends LoggerOptionT[F]

}
