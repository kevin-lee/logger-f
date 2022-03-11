---
id: log
title: "Log - Cats"
---

## Log - Cats (WIP)

`Log` is a typeclass to log `F[A]`, `F[Option[A]]`, `F[Either[A, B]]`, `OptionT[F, A]` and `EitherT[F, A, B]`.

It requires `Fx` from [Effectie](https://kevin-lee.github.io/effectie) and `Monad` from [Cats](https://typelevel.org/cats).

## Log `F[A]`
```scala
Log[F].log(F[A])(A => LogMessage)
```

A given `F[A]`, you can simply log `A` with `log`.


### Example

```scala
import cats._
import cats.syntax.all._
import cats.effect._

import effectie.cats._
import effectie.cats.Effectful._

import loggerf.cats._
import loggerf.logger._
import loggerf.syntax._

def hello[F[_]: Functor: Fx: Log](name: String): F[Unit] =
  log(pureOf(s"Hello $name"))(debug).map(println(_))
 
object MyApp extends IOApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- hello[IO]("World")
    _ <- hello[IO]("Kevin")
  } yield ExitCode.Success
}

```
```
23:34:25.021 [ioapp-compute-1] DEBUG MyApp - Hello World
Hello World
23:34:25.022 [ioapp-compute-1] DEBUG MyApp - Hello Kevin
Hello Kevin
```


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
import cats.effect._

import effectie.cats.Fx
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

  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =
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

```scala
Log[Option[F]].log(
  F[Option[A]]
)(
  ifEmpty: => LogMessage with MaybeIgnorable,
  toLeveledMessage: A => LogMessage with MaybeIgnorable
)
```

A given `F[Option[A]]`, you can simply log `Some(A)` or `None` with `log`.


### Example

```scala
import cats._
import cats.syntax.all._
import cats.effect._

import effectie.cats._
import effectie.cats.Effectful._

import loggerf.cats._
import loggerf.logger._
import loggerf.syntax._

def hello[F[_]: Functor: Fx: Log](name: Option[String]): F[Option[Unit]] = {
  log(pureOf(name))(warn("No name given"), a => info(s"Name: $a"))
    .map(maybeName => maybeName.map(name => println(s"Hello $name")))
}

object MyApp extends IOApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- hello[IO](none)
    _ <- hello[IO]("Kevin".some)
  } yield ExitCode.Success
}

```
```
23:42:22.584 [ioapp-compute-1] WARN MyApp - No name given
23:42:22.585 [ioapp-compute-1] INFO MyApp - Name: Kevin
Hello Kevin
```

## Log `F[Either[A, B]]`

## Log `OptionT[F, A]`

## Log `EitherT[F, A, B]`
