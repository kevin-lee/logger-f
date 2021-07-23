---
id: log
title: "Log - Monix"
---

## Log - Monix (WIP)

`Log` is a typeclass to log `F[A]`, `F[Option[A]]`, `F[Either[A, B]]`, `OptionT[F, A]` and `EitherT[F, A, B]`.

It requires `Fx` from [Effectie](https://kevin-lee.github.io/effectie) and `Monad` from [Cats](https://typelevel.org/cats).

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
import cats.syntax.all._
import cats.effect.ExitCode

import effectie.monix.{ConsoleEffect, Fx}
import effectie.monix.Effectful._

import loggerf.logger._
import loggerf.monix._
import loggerf.syntax._

import monix.eval.Task
import monix.eval.TaskApp


trait Greeting[F[_]] {
  def greet[A: Named](a: A): F[String]
}

object Greeting {
  def apply[F[_]: Greeting]: Greeting[F] = implicitly[Greeting[F]]

  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =
    new Greeting[F] {
      def greet[A: Named](a: A): F[String] =
        for {
          name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))
          greeting <- pureOf(s"Hello $name")
        } yield greeting
    }

}

object TaskMainApp extends TaskApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  def run(args: List[String]): Task[ExitCode] =
    for {
      greetingMessage <- Greeting[Task].greet(
        Person(GivenName("Kevin"), Surname("Lee"))
      )
      _ <- ConsoleEffect[Task].putStrLn(greetingMessage)
    } yield ExitCode.Success
}
```
```
19:57:04.076 [scala-execution-context-global-21] INFO MyApp - The name is Kevin Lee
Hello Kevin Lee
```

## Log `F[Option[A]]`

## Log `OptionT[F, A]`

## Log `F[Either[A, B]]`

## Log `EitherT[F, A, B]`
