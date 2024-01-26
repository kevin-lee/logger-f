---
sidebar_position: 1
id: getting-started
title: Getting Started
slug: /
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

![](/img/logger-f-200x200.png)

[![Build Status](https://github.com/Kevin-Lee/logger-f/workflows/Build-All/badge.svg)](https://github.com/Kevin-Lee/logger-f/actions?workflow=Build-All)
[![Release Status](https://github.com/Kevin-Lee/logger-f/workflows/Release/badge.svg)](https://github.com/Kevin-Lee/logger-f/actions?workflow=Release)
[![Latest version](https://index.scala-lang.org/kevin-lee/logger-f/latest.svg)](https://index.scala-lang.org/kevin-lee/logger-f)

| Project | Maven Central |
| ------: | ------------- |
| logger-f-cats | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-cats_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-cats_2.13) |
| logger-f-slf4j | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-slf4j_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-slf4j_2.13) |
| logger-f-log4j | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-log4j_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-log4j_2.13) |
| logger-f-log4s | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-log4s_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-log4s_2.13) |
| logger-f-sbt-logging | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-sbt-logging_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/logger-f-sbt-logging_2.13) |

* Supported Scala Versions: @SUPPORTED_SCALA_VERSIONS@

## LoggerF - Logger for `F[_]`

LoggerF is a tool for logging tagless final with an effect library. LoggerF requires [Effectie](https://kevin-lee.github.io/effectie) to construct `F[_]`. All the example code in this doc site uses Effectie so if you're not familiar with it, please check out [Effectie](https://kevin-lee.github.io/effectie) website.

Why LoggerF? Why not just log with `map` or `flatMap`? Please read ["Why?"](#why) section.

## Getting Started
### Get LoggerF For Cats Effect


### Get LoggerF For Cats
logger-f can be used wit any effect library or `Future` as long as there is an instance of `Fx` from effectie. Effectie provides instances of `Fx` for Cats Effect 2 and 3, and Monix 3.

#### With SLF4J
:::info
If you use logback, please use this.
:::

<Tabs
groupId="slf4j"
defaultValue="slf4j-sbt"
values={[
{label: 'sbt', value: 'slf4j-sbt'},
{label: 'sbt (with libraryDependencies)', value: 'slf4j-sbt-lib'},
{label: 'scala-cli', value: 'slf4j-scala-cli'},
]}>
<TabItem value="slf4j-sbt">

In `build.sbt`,

```scala
"io.kevinlee" %% "logger-f-cats" % "@VERSION@",
"io.kevinlee" %% "logger-f-slf4j" % "@VERSION@",
```

  </TabItem>

  <TabItem value="slf4j-sbt-lib">

In `build.sbt`,

```scala
libraryDependencies ++= Seq(
    "io.kevinlee" %% "logger-f-cats" % "@VERSION@",
    "io.kevinlee" %% "logger-f-slf4j" % "@VERSION@",
  )
```

  </TabItem>

  <TabItem value="slf4j-scala-cli">

```scala
//> using dep "io.kevinlee::logger-f-cats:@VERSION@"
//> using dep "io.kevinlee::logger-f-slf4j:@VERSION@"
```

  </TabItem>
</Tabs>


#### With Log4j

<Tabs
groupId="log4j"
defaultValue="log4j-sbt"
values={[
{label: 'sbt', value: 'log4j-sbt'},
{label: 'sbt (with libraryDependencies)', value: 'log4j-sbt-lib'},
{label: 'scala-cli', value: 'log4j-scala-cli'},
]}>
<TabItem value="log4j-sbt">

In `build.sbt`,

```scala
"io.kevinlee" %% "logger-f-cats" % "@VERSION@",
"io.kevinlee" %% "logger-f-log4j" % "@VERSION@",
```

  </TabItem>

  <TabItem value="log4j-sbt-lib">

In `build.sbt`,

```scala
libraryDependencies ++= Seq(
    "io.kevinlee" %% "logger-f-cats" % "@VERSION@",
    "io.kevinlee" %% "logger-f-log4j" % "@VERSION@",
  )
```

  </TabItem>

  <TabItem value="log4j-scala-cli">

```scala
//> using dep "io.kevinlee::logger-f-cats:@VERSION@"
//> using dep "io.kevinlee::logger-f-log4j:@VERSION@"
```

  </TabItem>
</Tabs>


#### With Log4s

<Tabs
groupId="log4s"
defaultValue="log4s-sbt"
values={[
{label: 'sbt', value: 'log4s-sbt'},
{label: 'sbt (with libraryDependencies)', value: 'log4s-sbt-lib'},
{label: 'scala-cli', value: 'log4s-scala-cli'},
]}>
<TabItem value="log4s-sbt">

In `build.sbt`,

```scala
"io.kevinlee" %% "logger-f-cats" % "@VERSION@",
"io.kevinlee" %% "logger-f-log4s" % "@VERSION@",
```

  </TabItem>

  <TabItem value="log4s-sbt-lib">

In `build.sbt`,

```scala
libraryDependencies ++= Seq(
    "io.kevinlee" %% "logger-f-cats" % "@VERSION@",
    "io.kevinlee" %% "logger-f-log4s" % "@VERSION@",
  )
```

  </TabItem>

  <TabItem value="log4s-scala-cli">

```scala
//> using dep "io.kevinlee::logger-f-cats:@VERSION@"
//> using dep "io.kevinlee::logger-f-log4s:@VERSION@"
```

  </TabItem>
</Tabs>


#### With sbt Logging Util
For sbt plugin development,

<Tabs
groupId="sbt-logging"
defaultValue="sbt-logging-sbt"
values={[
{label: 'sbt', value: 'sbt-logging-sbt'},
{label: 'sbt (with libraryDependencies)', value: 'sbt-logging-sbt-lib'},
{label: 'scala-cli', value: 'sbt-logging-scala-cli'},
]}>
<TabItem value="sbt-logging-sbt">

In `build.sbt`,

```scala
"io.kevinlee" %% "logger-f-cats" % "@VERSION@",
"io.kevinlee" %% "logger-f-sbt-logging" % "@VERSION@",
```

  </TabItem>

  <TabItem value="sbt-logging-sbt-lib">

In `build.sbt`,

```scala
libraryDependencies ++= Seq(
    "io.kevinlee" %% "logger-f-cats" % "@VERSION@",
    "io.kevinlee" %% "logger-f-sbt-logging" % "@VERSION@",
  )
```

  </TabItem>

  <TabItem value="sbt-logging-scala-cli">

```scala
//> using dep "io.kevinlee::logger-f-cats:@VERSION@"
//> using dep "io.kevinlee::logger-f-sbt-logging:@VERSION@"
```

  </TabItem>
</Tabs>


## Why
### Log without LoggerF
If you code tagless final and use some effect library like [Cats Effect](https://typelevel.org/cats-effect) and [Monix](https://monix.io) or use `Future`, you may have inconvenience in logging.

What inconvenience? I can just log with `flatMap` like.
```scala
for {
  a <- foo(n) // F[A]
  _ <- Sync[F].delay(logger.debug(s"a is $a")) // F[Unit]
  b <- bar(a) // F[B]
  _ <- Sync[F].delay(logger.debug(s"b is $b")) // F[Unit]
} yield b
```
That's true, but it's distracting to have log in each flatMap.
So,
```
1 line for the actual code
1 line for logging
1 line for the actual code
1 line for logging
```

### Log with LoggerF
It can be simplified by logger-f.
```scala
for {
  a <- foo(n).log(a => debug(s"a is $a")) // F[A]
  b <- bar(a).log(b => debug(s"b is $b")) // F[B]
} yield b
```
*** 

### Log without LoggerF (Option and OptionT)

What about `F[_]` with `Option` and `Either`? What happens if you want to use `Option` or `Either`? 
If you use `F[_]` with `Option` or `Either`, you may have more inconvenience or may not get the result you want.

e.g.)
```scala mdoc:reset-object
import cats.syntax.all._
import cats.effect._

import org.slf4j.LoggerFactory
val logger = LoggerFactory.getLogger("test-logger")

def foo[F[_]: Sync](n: Int): F[Option[Int]] = for {
  a <- Sync[F].pure(n.some)
  _ <- Sync[F].delay(
         a match {
           case Some(value) =>
             logger.debug(s"a is $value")
           case None =>
             logger.debug("No 'a' value found")
         }
       ) // F[Unit]
  b <- Sync[F].pure(none[Int])
  _ <- Sync[F].delay(
         b match {
           case Some(value) =>
             logger.debug(s"b is $value")
           case None =>
             () // don't log anything for None case
         }
       ) // F[Unit]
  c <- Sync[F].pure(123.some)
  _ <- Sync[F].delay(
         c match {
           case Some(value) =>
             () // don't log anything for None case
           case None =>
             logger.debug("No 'c' value found")
         }
       ) // F[Unit]
} yield c
```
So much noise for logging!

Now, let's think about the result.
```scala mdoc:nest
foo[IO](1).unsafeRunSync() // You probably want to have None here.

```

You expect `None` for the result due to `Sync[F].pure(none[Int])` yet you get `Some(123)` instead. That's because `b` is from `F[Option[Int]]` not from `Option[Int]`.

The same issue exists for `F[Either[A, B]]` as well.

So you need to use `OptionT` for `F[Option[A]]` and `EitherT` for `F[Either[A, B]]`.

Let's write it again with `OptionT`.

```scala mdoc:reset-object
import cats.data._
import cats.syntax.all._
import cats.effect._

import org.slf4j.LoggerFactory
val logger = LoggerFactory.getLogger("test-logger")

def foo[F[_]: Sync](n: Int): F[Option[Int]] = (for {
  a <- OptionT(Sync[F].pure(n.some))
  _ <- OptionT.liftF(Sync[F].delay(logger.debug(s"a is $a"))) // Now, you can't log None case.
  b <- OptionT(Sync[F].pure(none[Int]))
  _ <- OptionT.liftF(Sync[F].delay(logger.debug(s"b is $b"))) // You can't log None case.
  c <- OptionT(Sync[F].pure(123.some))
  _ <- OptionT.liftF(Sync[F].delay(logger.debug(s"c is $c"))) // You can't log None case.
} yield c).value
```
```scala mdoc:nest

foo[IO](1).unsafeRunSync() // You expect None here.
```
The problem's gone! Now each `flatMap` handles only `Some` case and that's what you want. However, because of that, it's hard to log `None` case.

***

### Log with LoggerF (Option and OptionT)

**LoggerF can solve this issue for you!**

```scala mdoc:reset-object
import cats._
import cats.data._
import cats.syntax.all._
import cats.effect._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

def foo[F[_]: Fx: Monad: Log](n: Int): F[Option[Int]] =
  (for {
    a <- OptionT(effectOf(n.some)).log(
           ifEmpty = error("a is empty"),
           a => debug(s"a is $a")
         )
    b <- OptionT(effectOf(none[Int])).log(
           error("b is empty"),
           b => debug(s"b is $b")
         )
    c <- OptionT(effectOf(123.some)).log(
           warn("c is empty"),
           c => debug(s"c is $c")
         )
  } yield c).value
```
```scala mdoc:nest
import loggerf.logger._

// or Slf4JLogger.slf4JLogger[MyClass]
implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyLogger")

import effectie.instances.ce2.fx._
import loggerf.instances.cats._

foo[IO](1).unsafeRunSync() // You expect None here.
```
With logs like
```
00:17:33.983 [main] DEBUG MyLogger - a is 1
00:17:33.995 [main] ERROR MyLogger - b is empty
```

***
### Log with LoggerF (EitherT)

Another example with `EitherT` (`F[Either[A, B]]` case is similar),
```scala mdoc:reset-object
import cats._
import cats.data._
import cats.syntax.all._
import cats.effect._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

def foo[F[_]: Fx: Monad: Log](n: Int): F[Either[String, Int]] =
  (for {
    a <- EitherT(effectOf(n.asRight[String])).log(
           err => error(s"Error: $err"),
           a => debug(s"a is $a")
         )
    b <- EitherT(effectOf("Some Error".asLeft[Int])).log(
           err => error(s"Error: $err"),
            b => debug(s"b is $b")
         )
    c <- EitherT(effectOf(123.asRight[String])).log(
           err => warn(s"Error: $err"),
           c => debug(s"c is $c")
         )
  } yield c).value
```
```scala mdoc:nest
import loggerf.logger._

// or Slf4JLogger.slf4JLogger[MyClass]
implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyLogger")

import effectie.instances.ce2.fx._
import loggerf.instances.cats._

foo[IO](1).unsafeRunSync() // You expect Left("Some Error") here.
```
With logs like
```
00:40:48.663 [main] DEBUG MyLogger - a is 1
00:40:48.667 [main] ERROR MyLogger - Error: Some Error
```

## Usage

Please check out
* [LoggerF for Cats](cats/cats.md)
