package loggerf.cats

import cats.data.{EitherT, OptionT}

import loggerf.cats.Log.LeveledMessage

/**
 * @author Kevin Lee
 */
trait Logful {
  def debugA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].debugA(fa)(a2String)

  def debugS[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].debugS(message)

  def infoA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].infoA(fa)(a2String)

  def infoS[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].infoS(message)

  def warnA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].warnA(fa)(a2String)

  def warnS[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].warnS(message)

  def errorA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].errorA(fa)(a2String)

  def errorS[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].errorS(message)

  def debugOption[F[_] : LoggerOption, A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    LoggerOption[F].debugOption(fa)(ifEmpty, a2String)

  def infoOption[F[_] : LoggerOption, A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    LoggerOption[F].infoOption(fa)(ifEmpty, a2String)

  def warnOption[F[_] : LoggerOption, A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    LoggerOption[F].warnOption(fa)(ifEmpty, a2String)

  def errorOption[F[_] : LoggerOption, A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    LoggerOption[F].errorOption(fa)(ifEmpty, a2String)

  def debugEither[F[_] : LoggerEither, A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    LoggerEither[F].debugEither(fab)(a2String, b2String)

  def infoEither[F[_] : LoggerEither, A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    LoggerEither[F].infoEither(fab)(a2String, b2String)

  def warnEither[F[_] : LoggerEither, A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    LoggerEither[F].warnEither(fab)(a2String, b2String)

  def errorEither[F[_] : LoggerEither, A, B](fab: F[Either[A, B]])(a2String: A => String, b2String: B => String): F[Either[A, B]] =
    LoggerEither[F].errorEither(fab)(a2String, b2String)


  def debugOptionT[F[_] : LoggerOptionT, A](fa: OptionT[F, A])(ifEmpty: => String, a2String: A => String): OptionT[F, A] =
    LoggerOptionT[F].debugOptionT(fa)(ifEmpty, a2String)

  def infoOptionT[F[_] : LoggerOptionT, A](fa: OptionT[F, A])(ifEmpty: => String, a2String: A => String): OptionT[F, A] =
    LoggerOptionT[F].infoOptionT(fa)(ifEmpty, a2String)

  def warnOptionT[F[_] : LoggerOptionT, A](fa: OptionT[F, A])(ifEmpty: => String, a2String: A => String): OptionT[F, A] =
    LoggerOptionT[F].warnOptionT(fa)(ifEmpty, a2String)

  def errorOptionT[F[_] : LoggerOptionT, A](fa: OptionT[F, A])(ifEmpty: => String, a2String: A => String): OptionT[F, A] =
    LoggerOptionT[F].errorOptionT(fa)(ifEmpty, a2String)


  def debugEitherT[F[_] : LoggerEitherT, A, B](efab: EitherT[F, A, B])(a2String: A => String, b2String: B => String): EitherT[F, A, B] =
    LoggerEitherT[F].debugEitherT(efab)(a2String, b2String)

  def infoEitherT[F[_] : LoggerEitherT, A, B](efab: EitherT[F, A, B])(a2String: A => String, b2String: B => String): EitherT[F, A, B] =
    LoggerEitherT[F].infoEitherT(efab)(a2String, b2String)

  def warnEitherT[F[_] : LoggerEitherT, A, B](efab: EitherT[F, A, B])(a2String: A => String, b2String: B => String): EitherT[F, A, B] =
    LoggerEitherT[F].warnEitherT(efab)(a2String, b2String)

  def errorEitherT[F[_] : LoggerEitherT, A, B](efab: EitherT[F, A, B])(a2String: A => String, b2String: B => String): EitherT[F, A, B] =
    LoggerEitherT[F].errorEitherT(efab)(a2String, b2String)


  def log[F[_] : Log, A](fa: F[A])(toLeveledMessage: A => LeveledMessage): F[A] =
    Log[F].log(fa)(toLeveledMessage)

  def log[F[_] : Log, A](
      foa: F[Option[A]]
    )(
      ifEmpty: => LeveledMessage
    , toLeveledMessage: A => LeveledMessage
    ): F[Option[A]] =
    Log[F].log(foa)(ifEmpty, toLeveledMessage)

  def log[F[_] : Log, A, B](
      feab: F[Either[A, B]]
    )(
      leftToMessage: A => LeveledMessage
    , rightToMessage: B => LeveledMessage
    ): F[Either[A, B]] =
    Log[F].log(feab)(leftToMessage, rightToMessage)

  def log[F[_] : Log, A](
      otfa: OptionT[F, A]
    )(
      ifEmpty: => LeveledMessage
    , toLeveledMessage: A => LeveledMessage
    ): OptionT[F, A] =
    Log[F].log(otfa)(ifEmpty, toLeveledMessage)


  def log[F[_] : Log, A, B](
      etfab: EitherT[F, A, B]
    )(
      leftToMessage: A => LeveledMessage
    , rightToMessage: B => LeveledMessage
    ): EitherT[F, A, B] =
    Log[F].log(etfab)(leftToMessage, rightToMessage)

}

object Logful extends Logful
