package loggerf.future

import loggerf.LeveledMessage
import loggerf.LeveledMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.core.Log

/** @author Kevin Lee
  * @since 2022-02-09
  */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
trait syntax {

  def log[F[_], A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
    L.log(fa)(toLeveledMessage)

  def logPure[F[_], A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
    L.logPure(fa)(toLeveledMessage)

  def log[F[_], A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LeveledMessage with MaybeIgnorable,
    toLeveledMessage: A => LeveledMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Option[A]] =
    L.log(foa)(ifEmpty, toLeveledMessage)

  def logPure[F[_], A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LeveledMessage with MaybeIgnorable,
    toLeveledMessage: A => LeveledMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Option[A]] =
    L.logPure(foa)(ifEmpty, toLeveledMessage)

  def log[F[_], A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LeveledMessage with MaybeIgnorable,
    rightToMessage: B => LeveledMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Either[A, B]] =
    L.log(feab)(leftToMessage, rightToMessage)

  def logPure[F[_], A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LeveledMessage with MaybeIgnorable,
    rightToMessage: B => LeveledMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Either[A, B]] =
    L.logPure(feab)(leftToMessage, rightToMessage)

}

object syntax extends syntax
