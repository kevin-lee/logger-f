package loggerf.core.syntax

import loggerf.LogMessage
import loggerf.LogMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.core.Log

/** @author Kevin Lee
  * @since 2022-02-09
  */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
trait LogSyntax {

  import LogSyntax._

  @inline def log[F[*], A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable)(
    implicit L: Log[F]
  ): F[A] =
    L.log(fa)(toLeveledMessage)

  @inline def log[F[*], A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Option[A]] =
    L.log(foa)(ifEmpty, toLeveledMessage)

  @inline def log[F[*], A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Either[A, B]] =
    L.log(feab)(leftToMessage, rightToMessage)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFOfASyntax[F[*], A](fa: F[A]): LogFOfASyntax[F, A] = new LogFOfASyntax[F, A](fa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFOfOptionSyntax[F[*], A](foa: F[Option[A]]): LogFOfOptionSyntax[F, A] =
    new LogFOfOptionSyntax[F, A](foa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def LogFOfEitherSyntax[F[*], A, B](feab: F[Either[A, B]]): LogFOfEitherSyntax[F, A, B] =
    new LogFOfEitherSyntax[F, A, B](feab)
}

object LogSyntax extends LogSyntax {
  class LogFOfASyntax[F[*], A](val fa: F[A]) extends AnyVal {
    @inline def log(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
      LogSyntax.log(fa)(toLeveledMessage)
  }

  class LogFOfOptionSyntax[F[*], A](val foa: F[Option[A]]) extends AnyVal {
    @inline def log(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Option[A]] =
      LogSyntax.log(foa)(ifEmpty, toLeveledMessage)
  }

  class LogFOfEitherSyntax[F[*], A, B](val feab: F[Either[A, B]]) extends AnyVal {
    @inline def log(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Either[A, B]] =
      LogSyntax.log(feab)(leftToMessage, rightToMessage)
  }
}
