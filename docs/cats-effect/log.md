---
id: log
title: "Log - Cats"
---

## Log - Cats (WIP)

`Log` is a typeclass to log `F[A]`, `F[Option[A]]`, `F[Either[A, B]]`, `OptionT[F, A]` and `EitherT[F, A, B]`.

It requires `EffectConstructor` from [Effectie](https://kevin-lee.github.io/effectie) and `Monad` from [Cats](https://typelevel.org/cats).

## Log `F[A]`
```scala
Log[F].log(F[A])(A => String)
```

### Example
```scala mdoc:reset-object
trait Named[A] {
  def name(a: A): String
}

object Named {
  def apply[A: Named]: Named[A] = implicitly[Named[A]]
}

final case class GivenName(givenName: String) extends AnyVal
final case class Surname(surname: String) extends AnyVal

final case class Person(givenName: GivenName, surname: Surname)
object Person {
  implicit val namedPerson: Named[Person] =
    person => s"${person.givenName.givenName} ${person.surname.surname}"
}

import cats._
import cats.implicits._
import cats.effect._

import effectie.cats.EffectConstructor
import effectie.cats.ConsoleEffect
import effectie.cats.Effectful._

import loggerf.cats._
import loggerf.logger._
import loggerf.syntax._

trait Greeting[F[_]] {
  def greet[A: Named](a: A): F[String]
}

object Greeting {
  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]

  implicit def hello[F[_]: EffectConstructor: Monad: Log]: Greeting[F] =
    new Greeting[F] {
      def greet[A: Named](a: A): F[String] = for {
        name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))
        greeting <- pureOf(s"Hello $name")
      } yield greeting
    }

}

object MyApp extends IOApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  def run(args: List[String]): IO[ExitCode] = for {
    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))
    _ <- ConsoleEffect[IO].putStrLn(greetingMessage)
  } yield ExitCode.Success
}

```
```
21:02:15.323 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee
Hello Kevin Lee
```

## Log `F[Option[A]]`

## Log `OptionT[F, A]`

## Log `F[Either[A, B]]`

## Log `EitherT[F, A, B]`
