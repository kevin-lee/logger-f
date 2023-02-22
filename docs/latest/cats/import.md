---
sidebar_position: 2
id: 'import'
title: "What to Import"
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# What to Import

## Most Places
```scala
import loggerf.core._
import loggerf.syntax.all._
```

```scala
import loggerf.core._ // for Log
import loggerf.syntax.all._ // for log(),  log_(), logS(), logS_(), etc.

def foo[F[_]: Log](n: Int): F[int] =
  for {
    n2 <- bar(n).log(n2 => info(s"n2: $n2"))
  } yield n + n2
```

## For Main Method
You need instances for `Log` when you actually run your program.
It's usually only one place where you put the `main` method.

### `Log[F]` Instance

For the instance of `Log[F]`,
```scala
import loggerf.instances.cats._
```

### Instances for Effectie

For the instance of `Fx[F]` (effectie)
<Tabs
  groupId="effectie"
  defaultValue="monix3"
  values={[
  {label: 'Monix 3', value: 'monix3'},
  {label: 'Cats Effect 2', value: 'ce2'},
  {label: 'Cats Effect 3', value: 'ce3'},
  ]}>
  <TabItem value="monix3">

```scala
import effectie.instnace.monix3.fx._
```

  </TabItem>

  <TabItem value="ce2">

```scala
import effectie.instnace.ce2.fx._
```

  </TabItem>

  <TabItem value="ce3">

```scala
import effectie.instnace.ce3.fx._
```

  </TabItem>
</Tabs>

### `CanLog` (`Logger`)

<Tabs
  groupId="loggers"
  defaultValue="slf4j"
  values={[
  {label: 'Slf4J', value: 'slf4j'},
  {label: 'Log4s', value: 'log4s'},
  {label: 'Log4j', value: 'log4j'},
  ]}>
  <TabItem value="slf4j">

If you use slf4j or logback, get `logger-f-slf4j` then,
```scala
import loggerf.logger._

implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[MyAppType]
// or
implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[this.type]

// or
implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("my-logger-name")

// or
implicit val canLog: CanLog = Slf4JLogger.slf4JCanLogWith(org.slf4j.LoggerFactory.getLogger(getClass))
```

  </TabItem>

  <TabItem value="log4s">

If you use log4s, get `logger-f-log4s` then,
```scala
import loggerf.logger._
implicit val canLog: CanLog = Log4sLogger.log4sCanLog[MyAppType]
// or
implicit val canLog: CanLog = Log4sLogger.log4sCanLog[this.type]

// or
implicit val canLog: CanLog = Log4sLogger.log4sCanLog("my-logger-name")

// or
implicit val canLog: CanLog = Log4sLogger.log4sCanLogWith(org.log4s.getLogger)
```
  </TabItem>

  <TabItem value="log4j">

If you use log4j, get `logger-f-log4j` then,
```scala

import loggerf.logger._
implicit val canLog: CanLog = Log4jLogger.log4jCanLog[MyAppType]
// or
implicit val canLog: CanLog = Log4jLogger.log4jCanLog[this.type]

// or
implicit val canLog: CanLog = Log4jLogger.log4jCanLog("my-logger-name")

// or
implicit val canLog: CanLog = Log4jLogger.log4jCanLogWith(org.apache.logging.log4j.LogManager.getLogger(getClass)) 
```

  </TabItem>
</Tabs>

## Example

```scala mdoc:reset-object
import cats._
import cats.syntax.all._

import effectie.core._
import effectie.syntax.all._

import loggerf.core._
import loggerf.syntax.all._

trait Foo[F[_]] {
  def foo(name: String): F[Unit]
}
object Foo {
  def apply[F[_]: Fx: Log: Monad]: Foo[F] = new FooF[F]
  
  private class FooF[F[_]: Fx: Log: Monad] extends Foo[F] {
    def foo(name: String): F[Unit] =
      for {
        _ <- s"Name: $name".logS_(info)
        message <- pureOf(s"Hello $name").log(infoA)
        _ <- effectOf(println(message))
      } yield ()
  }
}
```
```scala mdoc:compile-only
import cats.effect._

object MyApp extends IOApp.Simple {
  import loggerf.logger._ 
  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[this.type]

  import effectie.instances.ce2.fx._
  import loggerf.instances.cats._
  
  def run: IO[Unit] =
    Foo[IO].foo("Kevin")
    
}
```
