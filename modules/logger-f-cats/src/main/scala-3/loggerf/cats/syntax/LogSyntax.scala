package loggerf.cats.syntax

import cats.data.{EitherT, OptionT}
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.core.Log

/** @author Kevin Lee
  */
trait LogSyntax extends loggerf.core.syntax.LogSyntax {

  extension [F[*]: Log, A](otfa: OptionT[F, A]) {
    def log(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type
    ): OptionT[F, A] =
      OptionT(Log[F].log(otfa.value)(ifEmpty, toLeveledMessage))

    def logPure(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type
    ): OptionT[F, A] =
      OptionT(Log[F].logPure(otfa.value)(ifEmpty, toLeveledMessage))
  }

  extension [F[*]: Log, A, B](etfab: EitherT[F, A, B]) {
    def log(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type
    ): EitherT[F, A, B] =
      EitherT(Log[F].log(etfab.value)(leftToMessage, rightToMessage))

    def logPure(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type
    ): EitherT[F, A, B] =
      EitherT(Log[F].logPure(etfab.value)(leftToMessage, rightToMessage))
  }
}

object LogSyntax extends LogSyntax
