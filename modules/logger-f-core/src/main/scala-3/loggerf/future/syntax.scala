package loggerf.future

import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.core.Log
import loggerf.future.instances.given

import scala.concurrent.Future

/** @author Kevin Lee
  * @since 2022-02-09
  */
trait syntax {

  extension [F[*], A](fa: F[A]) {
    def log(toLeveledMessage: A => LeveledMessage)(using L: Log[F]): F[A] =
      L.log(fa)(toLeveledMessage)

    def logPure(toLeveledMessage: A => LeveledMessage)(using L: Log[F]): F[A] =
      L.logPure(fa)(toLeveledMessage)
  }

  extension [F[*], A](foa: F[Option[A]]) {
    def log(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type
    )(
      using L: Log[F]
    ): F[Option[A]] =
      L.log(foa)(ifEmpty, toLeveledMessage)

    def logPure(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type
    )(
      using L: Log[F]
    ): F[Option[A]] =
      L.logPure(foa)(ifEmpty, toLeveledMessage)
  }

  extension [F[*], A, B](feab: F[Either[A, B]]) {
    def log(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type
    )(
      using L: Log[F]
    ): F[Either[A, B]] =
      L.log(feab)(leftToMessage, rightToMessage)

    def logPure(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type
    )(
      using L: Log[F]
    ): F[Either[A, B]] =
      L.logPure(feab)(leftToMessage, rightToMessage)
  }
}

object syntax extends syntax
