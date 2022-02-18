package loggerf.cats

import _root_.cats.data.{EitherT, OptionT}
import loggerf.LogMessage
import loggerf.LogMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.core.Log

/** @author Kevin Lee
  */
trait syntax {

  import syntax._

  def log[F[_]: Log, A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable): F[A] =
    Log[F].log(fa)(toLeveledMessage)

  def logPure[F[_]: Log, A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable): F[A] =
    Log[F].logPure(fa)(toLeveledMessage)

  def log[F[_]: Log, A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): F[Option[A]] =
    Log[F].log(foa)(ifEmpty, toLeveledMessage)

  def logPure[F[_]: Log, A](
    foa: F[Option[A]],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): F[Option[A]] =
    Log[F].logPure(foa)(ifEmpty, toLeveledMessage)

  def log[F[_]: Log, A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): F[Either[A, B]] =
    Log[F].log(feab)(leftToMessage, rightToMessage)

  def logPure[F[_]: Log, A, B](
    feab: F[Either[A, B]],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): F[Either[A, B]] =
    Log[F].logPure(feab)(leftToMessage, rightToMessage)

  def log[F[_]: Log, A](
    otfa: OptionT[F, A],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): OptionT[F, A] =
    OptionT(Log[F].log(otfa.value)(ifEmpty, toLeveledMessage))

  def logPure[F[_]: Log, A](
    otfa: OptionT[F, A],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): OptionT[F, A] =
    OptionT(Log[F].logPure(otfa.value)(ifEmpty, toLeveledMessage))

  def log[F[_]: Log, A, B](
    etfab: EitherT[F, A, B],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): EitherT[F, A, B] =
    EitherT(Log[F].log(etfab.value)(leftToMessage, rightToMessage))

  def logPure[F[_]: Log, A, B](
    etfab: EitherT[F, A, B],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): EitherT[F, A, B] =
    EitherT(Log[F].logPure(etfab.value)(leftToMessage, rightToMessage))

  // /

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFaSyntax[F[_], A](fa: F[A]): LogFASyntax[F, A] = new LogFASyntax[F, A](fa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFOfOptionASyntax[F[_], A](foa: F[Option[A]]): LogFOfOptionASyntax[F, A] =
    new LogFOfOptionASyntax[F, A](foa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logFOfEitherABSyntax[F[_], A, B](feab: F[Either[A, B]]): LogFOfEitherABSyntax[F, A, B] =
    new LogFOfEitherABSyntax[F, A, B](feab)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logOptionTFASyntax[F[_], A](otfa: OptionT[F, A]): LogOptionTFASyntax[F, A] =
    new LogOptionTFASyntax[F, A](otfa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def LogEitherTFABSyntax[F[_], A, B](etfab: EitherT[F, A, B]): LogEitherTFABSyntax[F, A, B] =
    new LogEitherTFABSyntax[F, A, B](etfab)

}

object syntax extends syntax {
  final class LogFASyntax[F[_], A](val fa: F[A]) extends AnyVal {

    def log(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
      syntax.log(fa)(toLeveledMessage)

    def logPure(toLeveledMessage: A => LogMessage with NotIgnorable)(implicit L: Log[F]): F[A] =
      syntax.logPure(fa)(toLeveledMessage)

  }

  final class LogFOfOptionASyntax[F[_], A](val foa: F[Option[A]]) extends AnyVal {
    def log(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): F[Option[A]] =
      syntax.log(foa)(ifEmpty, toLeveledMessage)

    def logPure(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): F[Option[A]] =
      syntax.logPure(foa)(ifEmpty, toLeveledMessage)
  }

  final class LogFOfEitherABSyntax[F[_], A, B](val feab: F[Either[A, B]]) extends AnyVal {
    def log(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): F[Either[A, B]] =
      syntax.log(feab)(leftToMessage, rightToMessage)

    def logPure(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): F[Either[A, B]] =
      syntax.logPure(feab)(leftToMessage, rightToMessage)
  }

  final class LogOptionTFASyntax[F[_], A](val otfa: OptionT[F, A]) extends AnyVal {
    def log(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): OptionT[F, A] =
      OptionT(syntax.log(otfa.value)(ifEmpty, toLeveledMessage))

    def logPure(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): OptionT[F, A] =
      OptionT(syntax.logPure(otfa.value)(ifEmpty, toLeveledMessage))
  }

  final class LogEitherTFABSyntax[F[_], A, B](val etfab: EitherT[F, A, B]) extends AnyVal {
    def log(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): EitherT[F, A, B] =
      EitherT(syntax.log(etfab.value)(leftToMessage, rightToMessage))

    def logPure(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): EitherT[F, A, B] =
      EitherT(syntax.logPure(etfab.value)(leftToMessage, rightToMessage))

  }
}
