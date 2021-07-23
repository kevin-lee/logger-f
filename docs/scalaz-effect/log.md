---
id: log
title: "Log - Scalaz"
---
## Log - Scalaz (WIP)

`Log` is a typeclass to log `F[A]`, `F[Option[A]]`, `F[A \/ B]`, `OptionT[F, A]` and `EitherT[F, A, B]`.

It requires `Fx` from [Effectie](https://kevin-lee.github.io/effectie) and `Monad` from [Scalaz](https://github.com/scalaz/scalaz).

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

import scalaz._
import Scalaz._
import scalaz.effect._

import effectie.scalaz.Fx
import effectie.scalaz.ConsoleEffect
import effectie.scalaz.Effectful._

import loggerf.logger._
import loggerf.scalaz._
import loggerf.syntax._

trait Greeting[F[_]] {
  def greet[A: Named](a: A): F[String]
}

object Greeting {
  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]

  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =
    new Greeting[F] {
      def greet[A: Named](a: A): F[String] = for {
        name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))
        greeting <- pureOf(s"Hello $name")
      } yield greeting
    }

}

object MyApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  def run(args: List[String]): IO[Unit] = for {
    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))
    _ <- ConsoleEffect[IO].putStrLn(greetingMessage)
  } yield ()

  def main(args: Array[String]): Unit =
    run(args.toList).unsafePerformIO()
}

```
```
21:02:15.323 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee
Hello Kevin Lee
```

## Log `F[Option[A]]`

## Log `OptionT[F, A]`

## Log `F[A \/ B]`

## Log `EitherT[F, A, B]`
