---
id: getting-started
title: Getting Started
---

[![Build Status](https://github.com/Kevin-Lee/logger-f/workflows/Build%20All/badge.svg)](https://github.com/Kevin-Lee/logger-f/actions?workflow=Build+All)
[![Release Status](https://github.com/Kevin-Lee/logger-f/workflows/Release/badge.svg)](https://github.com/Kevin-Lee/logger-f/actions?workflow=Release)
[![Latest version](https://index.scala-lang.org/kevin-lee/logger-f/latest.svg)](https://index.scala-lang.org/kevin-lee/logger-f)

| Project | Bintray | Maven Central |
| ------: | ------- | ------------- |
| logger-f-cats-effect | [![Download](https://api.bintray.com/packages/kevinlee/maven/logger-f-cats-effect/images/download.svg)](https://bintray.com/kevinlee/maven/logger-f-cats-effect/_latestVersion) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-cats-effect_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-cats-effect_2.13) |
| logger-f-scalaz-effect | [![Download](https://api.bintray.com/packages/kevinlee/maven/logger-f-scalaz-effect/images/download.svg)](https://bintray.com/kevinlee/maven/logger-f-scalaz-effect/_latestVersion) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-scalaz-effect_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-scalaz-effect_2.13) |
| logger-f-slf4j | [![Download](https://api.bintray.com/packages/kevinlee/maven/logger-f-slf4j/images/download.svg)](https://bintray.com/kevinlee/maven/logger-f-slf4j/_latestVersion) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-slf4j_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-slf4j_2.13) |
| logger-f-log4j | [![Download](https://api.bintray.com/packages/kevinlee/maven/logger-f-log4j/images/download.svg)](https://bintray.com/kevinlee/maven/logger-f-log4j/_latestVersion) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-log4j_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-log4j_2.13) |
| logger-f-sbt-logging | [![Download](https://api.bintray.com/packages/kevinlee/maven/logger-f-sbt-logging/images/download.svg)](https://bintray.com/kevinlee/maven/logger-f-sbt-logging/_latestVersion) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-sbt-logging_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-sbt-logging_2.13) |

## ![](../img/logger-f-logo-96x96.png) LoggerF - Logger for `F[_]`

LoggerF is a tool for logging tagless final with an effect library. LoggerF requires [Effectie](https://kevin-lee.github.io/effectie) to construct `F[_]`. All the example code in this doc site uses Effectie so if you're not familiar with it, please check out [Effectie](https://kevin-lee.github.io/effectie) website.

Why LoggerF? Why not just log with `map` or `flatMap`? Please read ["Why?"](#why) section.

## Getting Started
### Get LoggerF For Cats Effect
#### With SLF4J

In `build.sbt`,

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "1.0.0",
    "io.kevinlee" %% "logger-f-slf4j" % "1.0.0"
  )
```

#### With Log4j

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "1.0.0",
    "io.kevinlee" %% "logger-f-log4j" % "1.0.0"
  )
```

#### With sbt Logging Util
You probably need `logger-f` for sbt plugin development.

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "1.0.0",
    "io.kevinlee" %% "logger-f-sbt-logging" % "1.0.0"
  )
```


### Get LoggerF For Scalaz Effect
#### With SLF4J

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "logger-f-scalaz-effect" % "1.0.0",
    "io.kevinlee" %% "logger-f-slf4j" % "1.0.0"
  )
```

#### With Log4j

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "logger-f-scalaz-effect" % "1.0.0",
    "io.kevinlee" %% "logger-f-log4j" % "1.0.0"
  )
```

#### With sbt Logging Util

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "logger-f-scalaz-effect" % "1.0.0",
    "io.kevinlee" %% "logger-f-sbt-logging" % "1.0.0"
  )
```

## Why
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

import loggerf.cats._
import loggerf.logger._
import loggerf.syntax._

// or Slf4JLogger.slf4JLogger[MyClass]
implicit val logger = Slf4JLogger.slf4JLogger("MyLogger")

def foo[F[_] : EffectConstructor : Monad : Log](n: Int): F[Option[Int]] =
  (for {
    a <- log(OptionT(effectOf(n.some)))(
        ifEmpty = error("a is empty"),
        a => debug(s"a is $a")
      )
    b <- log(OptionT(effectOf(none[Int])))(
        error("b is empty"),
        b => debug(s"b is $b")
      )
    c <- log(OptionT(effectOf(123.some)))(
        warn("c is empty"),
        c => debug(s"c is $c")
      )
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

import loggerf.cats._
import loggerf.logger._
import loggerf.syntax._

// or Slf4JLogger.slf4JLogger[MyClass]
implicit val logger = Slf4JLogger.slf4JLogger("MyLogger")

def foo[F[_] : EffectConstructor : Monad : Log](n: Int): F[Either[String, Int]] =
  (for {
    a <- log(EitherT(effectOf(n.asRight[String])))(
        err => error(s"Error: $err"),
        a => debug(s"a is $a")
      )
    b <- log(EitherT(effectOf("Some Error".asLeft[Int])))(
        err => error(s"Error: $err"),
         b => debug(s"b is $b")
      )
    c <- log(EitherT(effectOf(123.asRight[String])))(
        err => warn(s"Error: $err"),
        c => debug(s"c is $c")
      )
  } yield c).value

foo[IO](1).unsafeRunSync() // You expect Left("Some Error") here.
```
With logs like
```
00:40:48.663 [main] DEBUG MyLogger - a is 1
00:40:48.667 [main] ERROR MyLogger - Error: Some Error
```

### Usage

Pleae check out
* [LoggerF for Cats Effect](cats-effect/cats-effect)
* [LoggerF for Scalaz Effect](scalaz-effect/scalaz-effect)
