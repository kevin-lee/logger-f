package loggerf.core
import effectie.core.FxCtor
import loggerf.core.LogForTesting.Identity
import loggerf.logger.{CanLog, LoggerForTesting}

import scala.util.Try
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2022-02-19
  */
final case class LogForTesting(canLog0: LoggerForTesting) extends Log[Identity] {
  override implicit val EF: FxCtor[Identity] = LogForTesting.FxCtorForTesting

  override def flatMap0[A, B](fa: Identity[A])(f: A => Identity[B]): Identity[B] = f(fa)

  override def canLog: CanLog = canLog0
}
object LogForTesting {
  type Identity[A] = A

  implicit object FxCtorForTesting extends FxCtor[Identity] {
    override def effectOf[A](a: => A): Identity[A] = a

    override def pureOf[A](a: A): Identity[A] = a

    @SuppressWarnings(Array("org.wartremover.warts.Throw"))
    override def pureOrError[A](a: => A): Identity[A] =
      try a
      catch {
        case NonFatal(ex) => throw ex // scalafix: ok Disable:throw
      }

    override def unitOf: Identity[Unit] = ()

    @SuppressWarnings(Array("org.wartremover.warts.Throw"))
    override def errorOf[A](throwable: Throwable): Identity[A] = throw throwable

    override def fromEither[A](either: Either[Throwable, A]): Identity[A] = either.fold(errorOf, pureOf)

    override def fromOption[A](option: Option[A])(orElse: => Throwable): Identity[A] =
      option.fold(errorOf(orElse))(pureOf)

    override def fromTry[A](tryA: Try[A]): Identity[A] = tryA.fold(errorOf, pureOf)
  }

}
