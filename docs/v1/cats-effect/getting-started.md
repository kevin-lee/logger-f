---
id: 'getting-started'
title: "Get LoggerF"
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

## Get LoggerF for Cats Effect

In `build.sbt`,

<Tabs
groupId="cats-effect"
defaultValue="cats-effect"
values={[
{label: 'Cats Effect 3', value: 'cats-effect3'},
{label: 'Cats Effect 2', value: 'cats-effect'},
]}>
<TabItem value="cats-effect3">

```scala
"io.kevinlee" %% "logger-f-cats-effect3" % "@VERSION@"
```

  </TabItem>

  <TabItem value="cats-effect">

```scala
"io.kevinlee" %% "logger-f-cats-effect" % "@VERSION@"
```

  </TabItem>
</Tabs>


### With SLF4J
To use `logger-f` with SLF4J, add the following logger

```scala
"io.kevinlee" %% "logger-f-slf4j" % "1.20.0"
```


### With Log4j
To use `logger-f` with Log4j, add the following logger

```scala
"io.kevinlee" %% "logger-f-log4j" % "@VERSION@"
```

### With Log4s
To use `logger-f` with Log4s, add the following logger

```scala
"io.kevinlee" %% "logger-f-log4s" % "@VERSION@"
```


### With sbt Logging Util
You probably need `logger-f` for sbt plugin development.

```scala
"io.kevinlee" %% "logger-f-sbt-logging" % "@VERSION@"
```


## [Log](log.md)
