package loggerf.cats

import _root_.cats.data.{EitherT, OptionT}
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.core.Log

/** @author Kevin Lee
  */
trait syntax {

  def log[F[_]: Log, A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable): F[A] =
    Log[F].log(fa)(toLeveledMessage)

  def logPure[F[_]: Log, A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable): F[A] =
    Log[F].logPure(fa)(toLeveledMessage)

  def log[F[_]: Log, A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LeveledMessage with MaybeIgnorable,
    toLeveledMessage: A => LeveledMessage with MaybeIgnorable,
  ): F[Option[A]] =
    Log[F].log(foa)(ifEmpty, toLeveledMessage)

  def logPure[F[_]: Log, A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LeveledMessage with MaybeIgnorable,
    toLeveledMessage: A => LeveledMessage with MaybeIgnorable,
  ): F[Option[A]] =
    Log[F].logPure(foa)(ifEmpty, toLeveledMessage)

  def log[F[_]: Log, A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LeveledMessage with MaybeIgnorable,
    rightToMessage: B => LeveledMessage with MaybeIgnorable,
  ): F[Either[A, B]] =
    Log[F].log(feab)(leftToMessage, rightToMessage)

  def logPure[F[_]: Log, A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LeveledMessage with MaybeIgnorable,
    rightToMessage: B => LeveledMessage with MaybeIgnorable,
  ): F[Either[A, B]] =
    Log[F].logPure(feab)(leftToMessage, rightToMessage)

  def log[F[_]: Log, A](
    otfa: OptionT[F, A],
  )(
    ifEmpty: => LeveledMessage with MaybeIgnorable,
    toLeveledMessage: A => LeveledMessage with MaybeIgnorable,
  ): OptionT[F, A] =
    OptionT(Log[F].log(otfa.value)(ifEmpty, toLeveledMessage))

  def logPure[F[_]: Log, A](
    otfa: OptionT[F, A],
  )(
    ifEmpty: => LeveledMessage with MaybeIgnorable,
    toLeveledMessage: A => LeveledMessage with MaybeIgnorable,
  ): OptionT[F, A] =
    OptionT(Log[F].logPure(otfa.value)(ifEmpty, toLeveledMessage))

  def log[F[_]: Log, A, B](
    etfab: EitherT[F, A, B],
  )(
    leftToMessage: A => LeveledMessage with MaybeIgnorable,
    rightToMessage: B => LeveledMessage with MaybeIgnorable,
  ): EitherT[F, A, B] =
    EitherT(Log[F].log(etfab.value)(leftToMessage, rightToMessage))

  def logPure[F[_]: Log, A, B](
    etfab: EitherT[F, A, B],
  )(
    leftToMessage: A => LeveledMessage with MaybeIgnorable,
    rightToMessage: B => LeveledMessage with MaybeIgnorable,
  ): EitherT[F, A, B] =
    EitherT(Log[F].logPure(etfab.value)(leftToMessage, rightToMessage))

}

object syntax extends syntax
