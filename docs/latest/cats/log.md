---
id: log
title: "Log - Cats"
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

## Log - Cats (WIP)

`Log` is an algebra to log `F[A]`, `F[Option[A]]`, `F[Either[A, B]]`, `OptionT[F, A]` and `EitherT[F, A, B]`. So `Log` provides abstraction of logging operations, and there can multiple interpretations if required.  

More precisely, it requires `Fx` from [Effectie](https://kevin-lee.github.io/effectie) and `Monad` from [Cats](https://typelevel.org/cats). So it can be used for whatever effect you want as long as there's an interpreter for `Fx` of the effect.

## Log `String`
LoggerF is mainly for `F[_]` but let's start with more simple logging case that is logging `String`.

<Tabs
  groupId="logS"
  defaultValue="syntax"
  values={[
    {label: 'With syntax', value: 'syntax'},
    {label: 'Without syntax', value: 'no-syntax'},
  ]}>
  <TabItem value="syntax">

```scala
logS(String)(debug) // F[String]
logS(String)(info)  // F[String]
logS(String)(warn)  // F[String]
logS(String)(error) // F[String]
// or
String.logS(debug) // F[String]
String.logS(info)  // F[String]
String.logS(warn)  // F[String]
String.logS(error) // F[String]
```

If you don't need to re-use the `String` value,
```scala
import loggerf.syntax.all._

logS_(String)(debug) // F[Unit]
logS_(String)(info)  // F[Unit]
logS_(String)(warn)  // F[Unit]
logS_(String)(error) // F[Unit]
// or
String.logS_(debug) // F[Unit]
String.logS_(info)  // F[Unit]
String.logS_(warn)  // F[Unit]
String.logS_(error) // F[Unit]
```
  </TabItem>
  
  <TabItem value="no-syntax">

```scala
import loggerf.core._

Log[F].logS(String)(debug) // F[String]
Log[F].logS(String)(info)  // F[String]
Log[F].logS(String)(warn)  // F[String]
Log[F].logS(String)(error) // F[String]
```

If you don't need to re-use the `String` value,
```scala
Log[F].logS_(String)(debug) // F[Unit]
Log[F].logS_(String)(info)  // F[Unit]
Log[F].logS_(String)(warn)  // F[Unit]
Log[F].logS_(String)(error) // F[Unit]
```

  </TabItem>
</Tabs>

### Example

<Tabs
  groupId="logS-example"
  defaultValue="syntax"
  values={[
    {label: 'With syntax', value: 'syntax'},
    {label: 'Without syntax', value: 'no-syntax'},
  ]}>
  <TabItem value="syntax">

```scala mdoc:reset-object
import cats._
import cats.syntax.all._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

def hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {
  greeting <- logS(s"[1] Hello $name")(info)  // F[String]
  _        <- effectOf(println(greeting)) // F[Unit]
  // Or
  greeting2 <- s"[2] Hello $name".logS(info)  // F[String]
  _         <- effectOf(println(greeting2)) // F[Unit]
} yield ()
```
```scala mdoc:nest
import cats.effect._
import effectie.instances.ce2.fx._
import loggerf.instances.cats._
import loggerf.logger._

implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")

hello[IO]("Kevin").unsafeRunSync()
```
```
22:52:36.970 [Thread-64] INFO test-logger - [1] Hello Kevin
22:52:36.977 [Thread-64] INFO test-logger - [2] Hello Kevin
```
***
If you don't need to re-use the `String` value,
```scala mdoc:reset-object
import cats._
import cats.syntax.all._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

def hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {
  _ <- logS_(s"[1] The name is $name")(info) // F[Unit]
  // Or
  _ <- s"[2] The name is $name".logS_(info)  // F[Unit]
  
  _ <- effectOf(println(s"Hello $name")) // F[Unit]
} yield ()
```
```scala mdoc:nest
import cats.effect._
import effectie.instances.ce2.fx._
import loggerf.instances.cats._
import loggerf.logger._

implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")

hello[IO]("Kevin").unsafeRunSync()
```
```
22:53:09.165 [Thread-66] INFO test-logger - [1] The name is Kevin
22:53:09.166 [Thread-66] INFO test-logger - [2] The name is Kevin
```
  </TabItem>
  
  <TabItem value="no-syntax">

```scala mdoc:reset-object
import cats._
import cats.syntax.all._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

def hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {
  greeting <- Log[F].logS(s"Hello $name")(info) // F[String]
  _        <- effectOf(println(greeting))       // F[Unit]
} yield ()
```
```scala mdoc:nest
import cats.effect._
import effectie.instances.ce2.fx._
import loggerf.instances.cats._
import loggerf.logger._

implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")

hello[IO]("Kevin").unsafeRunSync()
```
```
02:07:09.298 [Thread-67] INFO test-logger - Hello Kevin
```

***
If you don't need to re-use the `String` value,
```scala mdoc:reset-object
import cats._
import cats.syntax.all._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

def hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {
  _ <- logS_(s"The name is $name")(info) // F[Unit]
  _ <- effectOf(println(s"Hello $name")) // F[Unit]
} yield ()
```
```scala mdoc:nest
import cats.effect._
import effectie.instances.ce2.fx._
import loggerf.instances.cats._
import loggerf.logger._

implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")

hello[IO]("Kevin").unsafeRunSync()
```
```
02:15:41.367 [Thread-74] INFO test-logger - The name is Kevin
```


  </TabItem>
</Tabs>



## Log `F[A]`
```scala
Log[F].log(F[A])(A => LogMessage) // F[A]
```
or with `loggerf.syntax`
```scala
F[A].log(A => LogMessage) // F[A]
```


A given `F[A]`, you can simply log `A` with `log`.

<Tabs
  groupId="log-fa"
  defaultValue="syntax"
  values={[
    {label: 'With Syntax', value: 'syntax'},
    {label: 'Without Syntax', value: 'no-syntax'},
  ]}>
  <TabItem value="syntax">

```scala
import loggerf.syntax.all._

val fa: F[A] = ...
fa.log(a => debug(s"Some meesage: $a")) // F[A]
fa.log(a => info(s"Some meesage: $a"))  // F[A]
fa.log(a => warn(s"Some meesage: $a"))  // F[A]
fa.log(a => error(s"Some meesage: $a")) // F[A]
// OR
log(fa)(a => debug(s"Some meesage: $a")) // F[A]
log(fa)(a => info(s"Some meesage: $a"))  // F[A]
log(fa)(a => warn(s"Some meesage: $a"))  // F[A]
log(fa)(a => error(s"Some meesage: $a")) // F[A]
```

```scala
import effectie.core._
import loggerf.core._
import loggerf.syntax.all._

def count[F[_]: Fx: Log](): F[Count] =
  counter.currentCount() // F[Count]
    .log(count => info(s"Current count: $count")) // F[Count]
```

  </TabItem>
  
  <TabItem value="no-syntax">

```scala
val fa: F[A] = ...
Log[F].log(fa)(a => debug(s"Some meesage: $a")) // F[A]
Log[F].log(fa)(a => info(s"Some meesage: $a"))  // F[A]
Log[F].log(fa)(a => warn(s"Some meesage: $a"))  // F[A]
Log[F].log(fa)(a => error(s"Some meesage: $a")) // F[A]
```

```scala
import effectie.core._
import loggerf.core._
import loggerf.syntax.all._

def count[F[_]: Fx: Log](): F[Count] =
  Log[F].log(counter.currentCount())(count => info(s"Current count: $count"))
```

  </TabItem>
</Tabs>


### Example

```scala
import cats._
import cats.syntax.all._
import cats.effect._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._
import loggerf.logger._

def hello[F[_]: Functor: Fx: Log](name: String): F[Unit] =
  s"Hello $name".logS(debug).map(println(_))
 
object MyApp extends IOApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  import effectie.instances.ce2.fx._
  import loggerf.instances.cats._
  
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

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._
import loggerf.logger._

trait Greeting[F[_]] {
  def greet[A: Named](a: A): F[String]
}

object Greeting {
  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]

  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =
    new Greeting[F] {
      def greet[A: Named](a: A): F[String] = for {
        name     <- effectOf(Named[A].name(a)).log(x => info(s"The name is $x"))
        greeting <- pureOf(s"Hello $name").log(greet => debug(s"Greeting: $greet"))
      } yield greeting
    }

}

object MyApp extends IOApp {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")

  import effectie.instances.ce2.fx._
  import loggerf.instances.cats._
  import effectie.instances.console._
  
  def run(args: List[String]): IO[ExitCode] = for {
    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))
    _ <- ConsoleEffect[IO].putStrLn(greetingMessage)
  } yield ExitCode.Success
}

```
```
23:04:56.272 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee
23:04:56.273 [ioapp-compute-0] DEBUG MyApp - Greeting: Hello Kevin Lee
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

```scala mdoc:reset-object
import cats._
import cats.syntax.all._
import cats.effect._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._
import loggerf.logger._

def greeting[F[_]: Fx](name: String): F[String] =
  pureOf(s"Hello $name")

def hello[F[_]: Monad: Fx: Log](maybeName: Option[String]): F[Unit] =
  for {
    name    <- pureOf(maybeName).log(
                 warn("No name given"),
                 name => info(s"Name: $name")
               )
    message <- name.traverse(greeting[F]).log(ignore, msg => info(s"Message: $msg"))
    _       <- effectOf(message.foreach(msg => println(msg)))
  } yield ()


implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp- F[Option[A]]")

import effectie.instances.ce2.fx._
import loggerf.instances.cats._

def run(): IO[Unit] = for {
  _ <- hello[IO](none)
  _ <- hello[IO]("Kevin".some)
} yield ()

run().unsafeRunSync()
```
```
20:09:43.117 [Thread-31] WARN MyApp- F[Option[A]] - No name given
20:09:43.133 [Thread-31] INFO MyApp- F[Option[A]] - Name: Kevin
20:09:43.133 [Thread-31] INFO MyApp- F[Option[A]] - Message: Hello Kevin
```

## Log `F[Either[A, B]]`

```scala
Log[Either[F]].log(
  F[Either[A, B]]
)(
  leftToMessage: A => LeveledMessage with MaybeIgnorable,
  rightToMessage: B => LeveledMessage with MaybeIgnorable
)
```

A given `F[Either[A, B]]`, you can simply log `Left(A)` or `Right(B)` with `log`.


### Example

```scala mdoc:reset-object
import cats._
import cats.syntax.all._
import cats.effect._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._
import loggerf.logger._

def foo[F[_]: Fx](a: Int): F[Int] =
  pureOf(a * 2)

def divide[F[_]: Fx: CanHandleError](a: Int, b: Int): F[Either[String, Int]] =
  effectOf((a / b).asRight[String])
    .handleNonFatal{ err =>
      err.getMessage.asLeft[Int]
    }

def calculate[F[_]: Monad: Fx: CanHandleError: Log](n: Int): F[Unit] =
  for {
    a      <- foo(n).log(
                n => info(s"n: ${n.toString}")
              )
    result <- divide(1000, a).log(
                err => error(s"Error: $err"),
                r => info(s"Result: ${r.toString}")
              )
    _      <- effectOf(println(result.fold(err => s"Error: $err", r => s"1000 / ${a.toString} = ${r.toString}")))
  } yield ()


implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp - F[Either[A, B]]")

import effectie.instances.ce2.fx._
import loggerf.instances.cats._

def run(): IO[Unit] = for {
  _ <- calculate[IO](5)
  _ <- calculate[IO](0)
} yield ()

run().unsafeRunSync()
```
```
20:20:05.588 [Thread-47] INFO MyApp - F[Either[A, B]] - n: 10
20:20:05.593 [Thread-47] INFO MyApp - F[Either[A, B]] - Result: 100
20:20:05.595 [Thread-47] INFO MyApp - F[Either[A, B]] - n: 0
20:20:05.605 [Thread-47] ERROR MyApp - F[Either[A, B]] - Error: / by zero
```


## Log `OptionT[F, A]`

## Log `EitherT[F, A, B]`
