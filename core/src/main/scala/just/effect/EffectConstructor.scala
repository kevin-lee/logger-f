package just.effect

import cats.effect.IO

trait EffectConstructor[F[_]] {
  def effect[A](a: => A): F[A]
  def pureEffect[A](a: A): F[A]
  def unit: F[Unit]
}

object EffectConstructor {
  def apply[F[_] : EffectConstructor]: EffectConstructor[F] = implicitly[EffectConstructor[F]]

  implicit val ioEffectConstructor: EffectConstructor[IO] = new EffectConstructor[IO] {

    override def effect[A](a: => A): IO[A] = IO(a)

    override def pureEffect[A](a: A): IO[A] = IO.pure(a)

    override def unit: IO[Unit] = IO.unit
  }

}