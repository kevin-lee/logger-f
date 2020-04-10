package loggerf

import cats.data.OptionT

/**
 * @author Kevin Lee
 */
trait Logful {
  def debugA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].debugA(fa)(a2String)

  def debug[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].debug(message)

  def infoA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].infoA(fa)(a2String)

  def info[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].info(message)

  def warnA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].warnA(fa)(a2String)

  def warn[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].warn(message)

  def errorA[F[_] : LoggerA, A](fa: F[A])(a2String: A => String): F[A] =
    LoggerA[F].errorA(fa)(a2String)

  def error[F[_] : LoggerA](message: F[String]): F[String] =
    LoggerA[F].error(message)

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

}

object Logful extends Logful
