package loggerf.cats.syntax

import _root_.cats.data.{EitherT, OptionT}
import loggerf.LogMessage
import loggerf.LogMessage.MaybeIgnorable
import loggerf.core.Log
import loggerf.core.syntax.{LogSyntax => CoreLogSyntax}

/** @author Kevin Lee
  */
trait LogSyntax extends loggerf.core.syntax.LogSyntax {

  import LogSyntax._

  def log[F[*]: Log, A](
    otfa: OptionT[F, A],
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): OptionT[F, A] =
    OptionT(Log[F].log(otfa.value)(ifEmpty, toLeveledMessage))

  def log[F[*]: Log, A, B](
    etfab: EitherT[F, A, B],
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): EitherT[F, A, B] =
    EitherT(Log[F].log(etfab.value)(leftToMessage, rightToMessage))

  // /

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def logOptionTFASyntax[F[*], A](otfa: OptionT[F, A]): LogOptionTFASyntax[F, A] =
    new LogOptionTFASyntax[F, A](otfa)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def LogEitherTFABSyntax[F[*], A, B](etfab: EitherT[F, A, B]): LogEitherTFABSyntax[F, A, B] =
    new LogEitherTFABSyntax[F, A, B](etfab)

}

object LogSyntax extends LogSyntax {

  final class LogOptionTFASyntax[F[*], A](val otfa: OptionT[F, A]) extends AnyVal {
    def log(
      ifEmpty: => LogMessage with MaybeIgnorable,
      toLeveledMessage: A => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): OptionT[F, A] =
      OptionT(CoreLogSyntax.log(otfa.value)(ifEmpty, toLeveledMessage))
  }

  final class LogEitherTFABSyntax[F[*], A, B](val etfab: EitherT[F, A, B]) extends AnyVal {
    def log(
      leftToMessage: A => LogMessage with MaybeIgnorable,
      rightToMessage: B => LogMessage with MaybeIgnorable,
    )(implicit L: Log[F]): EitherT[F, A, B] =
      EitherT(CoreLogSyntax.log(etfab.value)(leftToMessage, rightToMessage))

  }
}
