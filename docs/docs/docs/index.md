---
layout: docs
title: Getting Started
---

# ![LoggerF Logo](/logger-f/img/logger-f-logo-96x96.png) LoggerF - Logger for `F[_]`
LoggerF is a tool for logging tagless final with an effect library. LoggerF requires [Effectie](https://kevin-lee.github.io/effectie) to construct `F[_]`. All the example code in this doc site uses Effectie so if you're not familiar with it, please check out [Effectie](https://kevin-lee.github.io/effectie) website.

Why LoggerF? Why not just log with `map` or `flatMap`? Please read ["Why?"](#why) section.

# Getting Started
## Get LoggerF For Cats Effect
### With SLF4J

In `build.sbt`,

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "0.4.0",
    "io.kevinlee" %% "logger-f-slf4j" % "0.4.0"
  )
```

### With Log4j

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "0.4.0",
    "io.kevinlee" %% "logger-f-log4j" % "0.4.0"
  )
```

### With sbt Logging Util

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "0.4.0",
    "io.kevinlee" %% "logger-f-sbt-logging" % "0.4.0"
  )
```


## Get LoggerF For Scalaz Effect
### With SLF4J

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "logger-f-scalaz-effect" % "0.2.0",
    "io.kevinlee" %% "logger-f-slf4j" % "0.2.0"
  )
```

### With Log4j

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "logger-f-scalaz-effect" % "0.2.0",
    "io.kevinlee" %% "logger-f-log4j" % "0.2.0"
  )
```

# Why
If you use some effect library like [Cats Effect](https://typelevel.org/cats-effect) or [Scalaz Effect](https://scalaz.github.io) and tagless final, you may have inconvenience in logging.

What inconvenience? I can just log with `flatMap` like.
```scala
for {
  a <- foo(n) // F[A]
  _ <- effectOf(logger.debug(s"blah blah $a"))
  b <- bar(a) // F[A]
} yield b
```
That's true but what happens if you want to use `Option` or `Either`? If you use them with tagless final, you may get the result you want.
e.g.)
```scala mdoc:reset-object
import cats._
import cats.implicits._
import cats.effect._

import effectie.Effectful._
import effectie.cats._

def foo[F[_] : EffectConstructor : Monad](n: Int): F[Option[Int]] = for {
  a <- effectOf(n.some)
  b <- effectOf(none[Int])
  c <- effectOf(123.some)
} yield c

foo[IO](1).unsafeRunSync() // You expect None here!!!

```

You expect `None` for the result due to `effectOf(none[Int])` yet you get `Some(123)` instead. That's because `b` is `Option[Int]` not `Int`.

The same issue exists for `F[Either[A, B]]` as well.

So you need to use `OptionT` for `F[Option[A]]` and `EitherT` for `F[Either[A, B]]`.

Let's write it again with `OptionT`.

```scala mdoc:reset-object
import cats._
import cats.data._
import cats.implicits._
import cats.effect._

import effectie.Effectful._
import effectie.cats._

def foo[F[_] : EffectConstructor : Monad](n: Int): F[Option[Int]] = (for {
  a <- OptionT(effectOf(n.some))
  b <- OptionT(effectOf(none[Int]))
  c <- OptionT(effectOf(123.some))
} yield c).value

foo[IO](1).unsafeRunSync() // You expect None here.

```
The problem's gone! Now each `flatMap` handles only `Some` case and that's what you want. However, because of that, it's hard to log `None` case.

LoggerF can solve this issue for you.

```scala mdoc:reset-object
import cats._
import cats.data._
import cats.implicits._
import cats.effect._

import effectie.Effectful._
import effectie.cats._

import loggerf.Slf4JLogger
import loggerf.cats.Log
import loggerf.cats.Log.LeveledMessage._
import loggerf.cats.Logful._

implicit val logger = Slf4JLogger.slf4JLogger("MyLogger") // or Slf4JLogger.slf4JLogger[MyClass]

def foo[F[_] : EffectConstructor : Monad : Log](n: Int): F[Option[Int]] = (for {
  a <- log(OptionT(effectOf(n.some)))(ifEmpty = error("a is empty"), a => debug(s"a is $a"))
  b <- log(OptionT(effectOf(none[Int])))(error("b is empty"), b => debug(s"b is $b"))
  c <- log(OptionT(effectOf(123.some)))(warn("c is empty"), c => debug(s"c is $c"))
} yield c).value

foo[IO](1).unsafeRunSync() // You expect None here.
```
With logs like
```
00:17:33.983 [main] DEBUG MyLogger - a is 1
00:17:33.995 [main] ERROR MyLogger - b is empty
```

***

Another example with `EitherT`,
```scala mdoc:reset-object
import cats._
import cats.data._
import cats.implicits._
import cats.effect._

import effectie.Effectful._
import effectie.cats._

import loggerf.Slf4JLogger
import loggerf.cats.Log
import loggerf.cats.Log.LeveledMessage._
import loggerf.cats.Logful._

implicit val logger = Slf4JLogger.slf4JLogger("MyLogger") // or Slf4JLogger.slf4JLogger[MyClass]

def foo[F[_] : EffectConstructor : Monad : Log](n: Int): F[Either[String, Int]] = (for {
  a <- log(EitherT(effectOf(n.asRight[String])))(err => error(s"Error: $err"), a => debug(s"a is $a"))
  b <- log(EitherT(effectOf("Some Error".asLeft[Int])))(err => error(s"Error: $err"), b => debug(s"b is $b"))
  c <- log(EitherT(effectOf(123.asRight[String])))(err => warn(s"Error: $err"), c => debug(s"c is $c"))
} yield c).value

foo[IO](1).unsafeRunSync() // You expect Left("Some Error") here.
```
With logs like
```
00:40:48.663 [main] DEBUG MyLogger - a is 1
00:40:48.667 [main] ERROR MyLogger - Error: Some Error
```

## Usage

Pleae check out
* [LoggerF for Cats Effect](cats-effect)
* [LoggerF for Scalaz Effect](scalaz-effect)
