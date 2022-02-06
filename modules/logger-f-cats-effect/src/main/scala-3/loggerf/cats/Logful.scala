package loggerf.cats

import cats.data.{EitherT, OptionT}
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, NotIgnorable}

/**
 * @author Kevin Lee
 */
trait Logful {
  def log[F[_] : Log, A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    Log[F].log(fa)(toLeveledMessage)

  def log[F[_] : Log, A](
      foa: F[Option[A]]
    )(
      ifEmpty: => LeveledMessage | Ignorable
    , toLeveledMessage: A => LeveledMessage | Ignorable
    ): F[Option[A]] =
    Log[F].log(foa)(ifEmpty, toLeveledMessage)

  def log[F[_] : Log, A, B](
      feab: F[Either[A, B]]
    )(
      leftToMessage: A => LeveledMessage | Ignorable
    , rightToMessage: B => LeveledMessage | Ignorable
    ): F[Either[A, B]] =
    Log[F].log(feab)(leftToMessage, rightToMessage)

  def log[F[_] : Log, A](
      otfa: OptionT[F, A]
    )(
      ifEmpty: => LeveledMessage | Ignorable
    , toLeveledMessage: A => LeveledMessage | Ignorable
    ): OptionT[F, A] =
    Log[F].log(otfa)(ifEmpty, toLeveledMessage)


  def log[F[_] : Log, A, B](
      etfab: EitherT[F, A, B]
    )(
      leftToMessage: A => LeveledMessage | Ignorable
    , rightToMessage: B => LeveledMessage | Ignorable
    ): EitherT[F, A, B] =
    Log[F].log(etfab)(leftToMessage, rightToMessage)

}

object Logful extends Logful
