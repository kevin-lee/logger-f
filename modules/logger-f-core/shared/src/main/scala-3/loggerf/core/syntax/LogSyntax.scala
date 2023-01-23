package loggerf.core.syntax

import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.core.Log
import loggerf.instances.future.given

import scala.concurrent.Future

/** @author Kevin Lee
  * @since 2022-02-09
  */
trait LogSyntax {

  extension [F[*], A](fa: F[A]) {
    def log(toLeveledMessage: A => LeveledMessage)(using L: Log[F]): F[A] =
      L.log(fa)(toLeveledMessage)

    def log_(toLeveledMessage: A => LeveledMessage)(using L: Log[F]): F[Unit] =
      L.log_(fa)(toLeveledMessage)
  }

  extension (message: => String) {
    def logS[F[*]](toLeveledMessage: (String => LeveledMessage) with LeveledMessage.Leveled)(
      using L: Log[F]
    ): F[String] =
      L.logS(message)(toLeveledMessage)

    def logS_[F[*]](toLeveledMessage: (String => LeveledMessage) with LeveledMessage.Leveled)(
      using L: Log[F]
    ): F[Unit] =
      L.logS_(message)(toLeveledMessage)
  }

  extension [F[*], A](foa: F[Option[A]]) {
    def log(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type,
    )(
      using L: Log[F]
    ): F[Option[A]] =
      L.log(foa)(ifEmpty, toLeveledMessage)

    def log_(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type,
    )(
      using L: Log[F]
    ): F[Unit] =
      L.log_(foa)(ifEmpty, toLeveledMessage)
  }

  extension [F[*], A, B](feab: F[Either[A, B]]) {
    def log(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type,
    )(
      using L: Log[F]
    ): F[Either[A, B]] =
      L.log(feab)(leftToMessage, rightToMessage)

    def log_(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type,
    )(
      using L: Log[F]
    ): F[Unit] =
      L.log_(feab)(leftToMessage, rightToMessage)
  }
}

object LogSyntax extends LogSyntax
