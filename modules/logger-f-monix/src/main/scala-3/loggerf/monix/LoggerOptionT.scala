package loggerf.monix

import cats.*
import cats.data.OptionT
import effectie.core.FxCtor
import loggerf.logger.CanLog

trait LoggerOptionT[F[_]] {

  given EF: FxCtor[F]
  given MF: Monad[F]

  given canLog: CanLog

  def debugOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].debugOption(ofa.value)(ifEmpty, a2String)
    )

  def infoOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].infoOption(ofa.value)(ifEmpty, a2String)
    )

  def warnOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].warnOption(ofa.value)(ifEmpty, a2String)
    )

  def errorOptionT[A](
    ofa: OptionT[F, A]
  )(
    ifEmpty: => String
  , a2String: A => String
  ): OptionT[F, A] =
    OptionT(
      LoggerOption[F].errorOption(ofa.value)(ifEmpty, a2String)
    )

}

object LoggerOptionT {
  def apply[F[_] : LoggerOptionT]: LoggerOptionT[F] = summon[LoggerOptionT[F]]

  given loggerOptionT[F[_]](
    using EF: FxCtor[F], MF: Monad[F], canLog: CanLog
  ): LoggerOptionT[F] = new LoggerOptionTF[F](EF, MF, canLog)

  final class LoggerOptionTF[F[_]](
    override val EF: FxCtor[F]
  , override val MF: Monad[F]
  , override val canLog: CanLog
  ) extends LoggerOptionT[F]

}
