---
layout: docs
title: Getting Started
---

# ![LoggerF Logo](/logger-f/img/logger-f-logo-96x96.png) LoggerF

Why LoggerF? Please read ["Why?"](#why) section.

# Getting Started
## Get LoggerF For Cats Effect
### With SLF4J

In `build.sbt`,

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "0.1.0",
    "io.kevinlee" %% "logger-f-slf4j" % "0.1.0"
  )
```

### With Log4j

```scala
libraryDependencies ++=
  Seq(
    "io.kevinlee" %% "logger-f-cats-effect" % "0.1.0",
    "io.kevinlee" %% "logger-f-log4j" % "0.1.0"
  )
```


## Get LoggerF For Scalaz Effect
### With SLF4J

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "effectie-scalaz-effect" % "0.3.0",
    "io.kevinlee" %% "logger-f-slf4j" % "0.1.0"
  )
```

### With Log4j

In `build.sbt`,

```scala
libraryDependencies ++= 
  Seq(
    "io.kevinlee" %% "effectie-scalaz-effect" % "0.3.0",
    "io.kevinlee" %% "logger-f-log4j" % "0.1.0"
  )
```

# Why

## Usage

Pleae check out
* [LoggerF for Cats Effect](cats-effect)
* [LoggerF for Scalaz Effect](scalaz-effect)
