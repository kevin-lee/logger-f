package loggerf.cats

import cats._
import cats.data.OptionT
import effectie.cats.Fx
import loggerf.logger.CanLog

trait LoggerOptionT[F[_]] {

  implicit val EF: Fx[F]
  implicit val MF: Monad[F]

  implicit def canLog: CanLog

  def debugOptionT[A](
    ofa: OptionT[F, A],
  )(
    ifEmpty: => String,
    a2String: A => String,
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].debugOption(ofa.value)(ifEmpty, a2String),
    )

  def infoOptionT[A](
    ofa: OptionT[F, A],
  )(
    ifEmpty: => String,
    a2String: A => String,
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].infoOption(ofa.value)(ifEmpty, a2String),
    )

  def warnOptionT[A](
    ofa: OptionT[F, A],
  )(
    ifEmpty: => String,
    a2String: A => String,
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].warnOption(ofa.value)(ifEmpty, a2String),
    )

  def errorOptionT[A](
    ofa: OptionT[F, A],
  )(
    ifEmpty: => String,
    a2String: A => String,
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].errorOption(ofa.value)(ifEmpty, a2String),
    )

}

object LoggerOptionT {
  def apply[F[_]: LoggerOptionT]: LoggerOptionT[F] = implicitly[LoggerOptionT[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerOptionT[F[_]](
    implicit EF: Fx[F],
    MF: Monad[F],
    canLog: CanLog,
  ): LoggerOptionT[F] = new LoggerOptionTF[F]

  final class LoggerOptionTF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: Fx[F],
    override val MF: Monad[F],
    override val canLog: CanLog,
  ) extends LoggerOptionT[F]

}
