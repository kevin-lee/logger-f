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

  @inline def log_[F[*], A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable)(
    implicit L: Log[F]
  ): F[Unit] =
    L.log_(fa)(toLeveledMessage)

  @inline def logS[F[*]](message: String)(toLeveledMessage: String => LogMessage with NotIgnorable)(
    implicit L: Log[F]
  ): F[String] =
    L.logS(message)(toLeveledMessage)

  @inline def logS_[F[*]](message: String)(toLeveledMessage: String => LogMessage with NotIgnorable)(
    implicit L: Log[F]
  ): F[Unit] =
    L.logS_(message)(toLeveledMessage)

  @inline def log[F[*], A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Option[A]] =
    L.log(foa)(ifEmpty, toLeveledMessage)

  @inline def log_[F[*], A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Unit] =
    L.log_(foa)(ifEmpty, toLeveledMessage)

  @inline def log[F[*], A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Either[A, B]] =
    L.log(feab)(leftToMessage, rightToMessage)

  @inline def log_[F[*], A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  )(
    implicit L: Log[F]
  ): F[Unit] =
    L.log_(feab)(leftToMessage, rightToMessage)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFOfASyntax[F[*], A](fa: F[A]): LogFOfASyntax[F, A] = new LogFOfASyntax[F, A](fa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFStringSyntax(message: String): LogFForStringSyntax = new LogFForStringSyntax(message)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFOfOptionSyntax[F[*], A](foa: F[Option[A]]): LogFOfOptionSyntax[F, A] =
    new LogFOfOptionSyntax[F, A](foa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def LogFOfEitherSyntax[F[*], A, B](feab: F[Either[A, B]]): LogFOfEitherSyntax[F, A, B] =
    new LogFOfEitherSyntax[F, A, B](feab)
}

object LogSyntax extends LogSyntax {
  final class LogFOfASyntax[F[*], A](private val fa: F[A]) extends AnyVal {
    @inline def log(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
      LogSyntax.log(fa)(toLeveledMessage)

    @inline def log_(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[Unit] =
      LogSyntax.log_(fa)(toLeveledMessage)
  }

  final class LogFForStringSyntax(private val message: String) extends AnyVal {
    @inline def logS[F[*]](toLeveledMessage: String => LogMessage with NotIgnorable)(implicit L: Log[F]): F[String] =
      LogSyntax.logS(message)(toLeveledMessage)

    @inline def logS_[F[*]](toLeveledMessage: String => LogMessage with NotIgnorable)(implicit L: Log[F]): F[Unit] =
      LogSyntax.logS_(message)(toLeveledMessage)
  }

  final class LogFOfOptionSyntax[F[*], A](private val foa: F[Option[A]]) extends AnyVal {
    @inline def log(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Option[A]] =
      LogSyntax.log(foa)(ifEmpty, toLeveledMessage)

    @inline def log_(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Unit] =
      LogSyntax.log_(foa)(ifEmpty, toLeveledMessage)
  }

  final class LogFOfEitherSyntax[F[*], A, B](private val feab: F[Either[A, B]]) extends AnyVal {
    @inline def log(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Either[A, B]] =
      LogSyntax.log(feab)(leftToMessage, rightToMessage)

    @inline def log_(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(
      implicit L: Log[F]
    ): F[Unit] =
      LogSyntax.log_(feab)(leftToMessage, rightToMessage)
  }
}
