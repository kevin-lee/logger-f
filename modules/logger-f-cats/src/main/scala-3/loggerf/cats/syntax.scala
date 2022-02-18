package loggerf.cats

import cats.data.{EitherT, OptionT}
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.core.Log

/** @author Kevin Lee
  */
trait syntax {
  extension [F[*]: Log, A] (fa: F[A]) {
    def log(toLeveledMessage: A => LeveledMessage): F[A] =
      Log[F].log(fa)(toLeveledMessage)

    def logPure(toLeveledMessage: A => LeveledMessage): F[A] =
      Log[F].logPure(fa)(toLeveledMessage)
  }

  extension [F[*]: Log, A] (foa: F[Option[A]]) {
    def log(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type
    ): F[Option[A]] =
      Log[F].log(foa)(ifEmpty, toLeveledMessage)

    def logPure(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type
    ): F[Option[A]] =
      Log[F].logPure(foa)(ifEmpty, toLeveledMessage)
  }

  extension [F[*]: Log, A, B] (feab: F[Either[A, B]]) {
    def log(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type
    ): F[Either[A, B]] =
      Log[F].log(feab)(leftToMessage, rightToMessage)

    def logPure(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type
    ): F[Either[A, B]] =
      Log[F].logPure(feab)(leftToMessage, rightToMessage)
  }

  extension [F[*]: Log, A] (otfa: OptionT[F, A]) {
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

  extension [F[*]: Log, A, B] (etfab: EitherT[F, A, B]) {
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

object syntax extends syntax
