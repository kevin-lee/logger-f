package loggerf.future

import loggerf.LogMessage
import loggerf.LogMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.core.Log

/** @author Kevin Lee
  * @since 2022-02-09
  */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
trait syntax {

  @inline def log[F[_], A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable)(
    implicit L: Log[F]
  ): F[A] =
    L.log(fa)(toLeveledMessage)

  @inline def logPure[F[_], A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable)(
    implicit L: Log[F]
  ): F[A] =
    L.logPure(fa)(toLeveledMessage)

  @inline def log[F[_], A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Option[A]] =
    L.log(foa)(ifEmpty, toLeveledMessage)

  @inline def logPure[F[_], A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Option[A]] =
    L.logPure(foa)(ifEmpty, toLeveledMessage)

  @inline def log[F[_], A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Either[A, B]] =
    L.log(feab)(leftToMessage, rightToMessage)

  @inline def logPure[F[_], A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Either[A, B]] =
    L.logPure(feab)(leftToMessage, rightToMessage)

}

object syntax extends syntax {
  implicit class LogFOfASyntax[F[_], A](val fa: F[A]) extends AnyVal {
    @inline def log(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
      syntax.log(fa)(toLeveledMessage)

    @inline def logPure(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
      syntax.logPure(fa)(toLeveledMessage)
  }

  implicit class LogFOfOptionSyntax[F[_], A](val foa: F[Option[A]]) extends AnyVal {
    @inline def log(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Option[A]] =
      syntax.log(foa)(ifEmpty, toLeveledMessage)

    @inline def logPure(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Option[A]] =
      syntax.logPure(foa)(ifEmpty, toLeveledMessage)
  }

  implicit class LogFOfEitherSyntax[F[_], A, B](val feab: F[Either[A, B]]) extends AnyVal {
    @inline def log(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Either[A, B]] =
      syntax.log(feab)(leftToMessage, rightToMessage)

    @inline def logPure(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Either[A, B]] =
      syntax.logPure(feab)(leftToMessage, rightToMessage)

  }
}
