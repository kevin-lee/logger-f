package loggerf.cats

import cats.data.{EitherT, OptionT}
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, NotIgnorable}
import loggerf.core.Log

/** @author Kevin Lee
  */
trait syntax {
  def log[F[*]: Log, A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    Log[F].log(fa)(toLeveledMessage)

  def logPure[F[*]: Log, A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    Log[F].logPure(fa)(toLeveledMessage)

  def log[F[*]: Log, A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): F[Option[A]] =
    Log[F].log(foa)(ifEmpty, toLeveledMessage)

  def logPure[F[*]: Log, A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): F[Option[A]] =
    Log[F].logPure(foa)(ifEmpty, toLeveledMessage)

  def log[F[*]: Log, A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): F[Either[A, B]] =
    Log[F].log(feab)(leftToMessage, rightToMessage)

  def logPure[F[*]: Log, A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): F[Either[A, B]] =
    Log[F].logPure(feab)(leftToMessage, rightToMessage)

  def log[F[*]: Log, A](
    otfa: OptionT[F, A]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): OptionT[F, A] =
    OptionT(Log[F].log(otfa.value)(ifEmpty, toLeveledMessage))

  def logPure[F[*]: Log, A](
    otfa: OptionT[F, A]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): OptionT[F, A] =
    OptionT(Log[F].logPure(otfa.value)(ifEmpty, toLeveledMessage))

  def log[F[*]: Log, A, B](
    etfab: EitherT[F, A, B]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): EitherT[F, A, B] =
    EitherT(Log[F].log(etfab.value)(leftToMessage, rightToMessage))

  def logPure[F[*]: Log, A, B](
    etfab: EitherT[F, A, B]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): EitherT[F, A, B] =
    EitherT(Log[F].logPure(etfab.value)(leftToMessage, rightToMessage))

}

object syntax extends syntax
