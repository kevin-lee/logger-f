package loggerf.future

import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, NotIgnorable}
import loggerf.core.Log
import loggerf.future.instances.given

import scala.concurrent.Future

/** @author Kevin Lee
  * @since 2022-02-09
  */
trait syntax {
  def log[F[*], A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable)(using L: Log[F]): F[A] =
    L.log(fa)(toLeveledMessage)

  def logPure[F[*], A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable)(using L: Log[F]): F[A] =
    L.logPure(fa)(toLeveledMessage)

  def log[F[*], A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  )(
    using L: Log[F]
  ): F[Option[A]] =
    L.log(foa)(ifEmpty, toLeveledMessage)

  def logPure[F[*], A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  )(
    using L: Log[F]
  ): F[Option[A]] =
    L.logPure(foa)(ifEmpty, toLeveledMessage)

  def log[F[*], A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  )(
    using L: Log[F]
  ): F[Either[A, B]] =
    L.log(feab)(leftToMessage, rightToMessage)

  def logPure[F[*], A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  )(
    using L: Log[F]
  ): F[Either[A, B]] =
    L.logPure(feab)(leftToMessage, rightToMessage)

}
object syntax extends syntax
