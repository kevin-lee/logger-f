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
  }

  extension [F[*], A](foa: F[Option[A]]) {
    def log(
      ifEmpty: => LeveledMessage | Ignore.type,
      toLeveledMessage: A => LeveledMessage | Ignore.type,
    )(
      using L: Log[F]
    ): F[Option[A]] =
      L.log(foa)(ifEmpty, toLeveledMessage)
  }

  extension [F[*], A, B](feab: F[Either[A, B]]) {
    def log(
      leftToMessage: A => LeveledMessage | Ignore.type,
      rightToMessage: B => LeveledMessage | Ignore.type,
    )(
      using L: Log[F]
    ): F[Either[A, B]] =
      L.log(feab)(leftToMessage, rightToMessage)
  }
}

object LogSyntax extends LogSyntax
