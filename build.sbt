import ProjectInfo.*
import just.semver.SemVer
import kevinlee.sbt.SbtCommon.crossVersionProps
import sbtcrossproject.CrossProject

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / organization := "io.kevinlee"
ThisBuild / organizationName := "Kevin's Code"
ThisBuild / crossScalaVersions := props.CrossScalaVersions

ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  )
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}").some
ThisBuild / scmInfo :=
  ScmInfo(
    browseUrl = url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
    connection = s"scm:git:git@github.com:${props.GitHubUsername}/${props.RepoName}.git",
  ).some

ThisBuild / licenses := props.licenses

//ThisBuild / semanticdbEnabled := true
//ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalafixConfig := (
  if (scalaVersion.value.startsWith("3"))
    ((ThisBuild / baseDirectory).value / ".scalafix-scala3.conf").some
  else
    ((ThisBuild / baseDirectory).value / ".scalafix-scala2.conf").some
)

//ThisBuild / scalafixScalaBinaryVersion                   := {
//  val log        = sLog.value
//  val newVersion = if (scalaVersion.value.startsWith("3")) {
//    (ThisBuild / scalafixScalaBinaryVersion).value
//  } else {
//    CrossVersion.binaryScalaVersion(scalaVersion.value)
//  }
//
//  log.info(
//    s">> Change ThisBuild / scalafixScalaBinaryVersion from ${(ThisBuild / scalafixScalaBinaryVersion).value} to $newVersion"
//  )
//  newVersion
//}

//ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.2.12"

lazy val loggerF = (project in file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin)
  .settings(
    name := prefixedProjectName(""),
    description := "Logger for F[_]",
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    )
    /* GitHub Release { */,
    devOopsPackagedArtifacts := List(s"*/*/*/target/scala-*/${devOopsArtifactNamePrefix.value}*.jar"),
    /* } GitHub Release */
  )
  .settings(noPublish)
  .aggregate(
    coreJvm,
    coreJs,
    coreNative,
    slf4jLoggerJvm,
    slf4jLoggerNative,
    log4sLoggerJvm,
    log4sLoggerJs,
    log4jLoggerJvm,
    log4jLoggerNative,
    sbtLoggingJvm,
    sbtLoggingNative,
    catsJvm,
    catsJs,
    catsNative,
    slf4jMdcJvm,
    slf4jMdcNative,
    logbackMdcMonix3Jvm,
    testLogbackMdcMonix3Jvm,
    logbackMdcCatsEffect3Jvm,
    doobie1Jvm,
    testKitJvm,
    testKitJs,
    testKitNative,
    catsEffectJvm,
//    catsEffectJs,
    catsEffect3Jvm,
//    catsEffect3Js,
    monixJvm,
//    monixJs,
  )

lazy val core       =
  module(ProjectName("core"), crossProject(JVMPlatform, JSPlatform, NativePlatform))
    .settings(
      description := "Logger for F[_] - Core",
      libraryDependencies ++= List(
        libs.effectieCore.value,
        libs.orphanCats.value,
        libs.cats.value % Test,
        libs.tests.extrasConcurrent.value,
        libs.tests.extrasConcurrentTesting,
        libs.cats.value % Optional,
      ) ++ libs.tests.hedgehogLibs.value,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
lazy val coreJvm    = core.jvm
lazy val coreJs     = core
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )
lazy val coreNative = core.native.settings(nativeSettings)

lazy val slf4jLogger       = module(ProjectName("slf4j"), crossProject(JVMPlatform, NativePlatform))
  .settings(
    description := "Logger for F[_] - Logger with Slf4j",
    libraryDependencies ++= Seq(
      libs.slf4jApi
    ),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
  )
  .dependsOn(core)
lazy val slf4jLoggerJvm    = slf4jLogger.jvm
lazy val slf4jLoggerNative = slf4jLogger.native.settings(nativeSettings)

lazy val log4sLogger    =
  module(ProjectName("log4s"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Logger with Log4s",
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
      libraryDependencies ++= List(
        libs.log4sLib.value
      ),
    )
    .dependsOn(core)
lazy val log4sLoggerJvm = log4sLogger.jvm
lazy val log4sLoggerJs  = log4sLogger
  .js
  .settings(
    jsSettings
  )

lazy val log4jLogger       =
  module(ProjectName("log4j"), crossProject(JVMPlatform, NativePlatform))
    .settings(
      description := "Logger for F[_] - Logger with Log4j",
      Compile / unmanagedSourceDirectories ++= {
        val sharedSourceDir = (baseDirectory.value / ".." / "shared").getCanonicalFile / "src" / "main"
        if (scalaVersion.value.startsWith("3."))
          Seq(
            sharedSourceDir / "scala-2.12_3.0",
            sharedSourceDir / "scala-2.13_3.0",
          )
        else if (scalaVersion.value.startsWith("2.13"))
          Seq(
            sharedSourceDir / "scala-2.12_2.13",
            sharedSourceDir / "scala-2.12_3.0",
            sharedSourceDir / "scala-2.13_3.0",
          )
        else if (scalaVersion.value.startsWith("2.12"))
          Seq(
            sharedSourceDir / "scala-2.12_2.13",
            sharedSourceDir / "scala-2.12_3.0",
            sharedSourceDir / "scala-2.12",
          )
        else
          Seq.empty
      },
      Test / unmanagedSourceDirectories ++= {
        val sharedSourceDir = (baseDirectory.value / ".." / "shared").getCanonicalFile / "src" / "test"
        if (scalaVersion.value.startsWith("3."))
          Seq(
            sharedSourceDir / "scala-2.12_3.0",
            sharedSourceDir / "scala-2.13_3.0",
          )
        else if (scalaVersion.value.startsWith("2.13"))
          Seq(
            sharedSourceDir / "scala-2.12_2.13",
            sharedSourceDir / "scala-2.13_3.0",
          )
        else if (scalaVersion.value.startsWith("2.12"))
          Seq(
            sharedSourceDir / "scala-2.12_2.13",
            sharedSourceDir / "scala-2.12_3.0",
            sharedSourceDir / "scala-2.12",
          )
        else
          Seq.empty
      },
      libraryDependencies ++= Seq(
        libs.log4jApi,
        libs.log4jCore,
      ).map(_ % Provided),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core)
lazy val log4jLoggerJvm    = log4jLogger.jvm
lazy val log4jLoggerNative = log4jLogger.native.settings(nativeSettings)

lazy val sbtLogging       =
  module(ProjectName("sbt-logging"), crossProject(JVMPlatform, NativePlatform))
    .settings(
      description := "Logger for F[_] - Logger with sbt logging",
      libraryDependencies ++= crossVersionProps(
        List.empty,
        SemVer.parseUnsafe(scalaVersion.value),
      ) {
        case (SemVer.Major(2), SemVer.Minor(11), _) =>
          List(
            libs.sbtLoggingLib("1.2.4")
          )

        case (SemVer.Major(2), SemVer.Minor(12), _) =>
          List(
            libs.sbtLoggingLib("1.5.8")
          )

        case (SemVer.Major(2), SemVer.Minor(13), _) | (SemVer.Major(3), SemVer.Minor(_), _) =>
          List(
            libs.sbtLoggingLib("1.5.8")
          ).map(_.cross(CrossVersion.for3Use2_13))
      },
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core)
lazy val sbtLoggingJvm    = sbtLogging.jvm
lazy val sbtLoggingNative = sbtLogging.native.settings(nativeSettings)

lazy val cats       =
  module(ProjectName("cats"), crossProject(JVMPlatform, JSPlatform, NativePlatform))
    .settings(
      description := "Logger for F[_] - Cats",
      libraryDependencies ++= libs.tests.hedgehogLibs.value ++ List(
        libs.effectieCore.value,
        libs.cats.value,
        libs.effectieSyntax.value,
        libs.effectieCats.value % Test,
      ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core % props.IncludeTest)
lazy val catsJvm    = cats.jvm
lazy val catsJs     = cats
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )
lazy val catsNative = cats.native.settings(nativeSettings)

lazy val slf4jMdc       = module(ProjectName("slf4j-mdc"), crossProject(JVMPlatform, NativePlatform))
  .settings(
    description := "Logger for F[_] - A tool to set MDC's MDCAdapter",
    libraryDependencies ++= Seq(
      libs.slf4jApi,
      libs.cats.value % Test,
    ) ++ libs.tests.hedgehogLibs.value,
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
  )
  .dependsOn(
    core
  )
lazy val slf4jMdcJvm    = slf4jMdc.jvm
lazy val slf4jMdcNative = slf4jMdc.native.settings(nativeSettings)

lazy val logbackMdcMonix3    =
  module(ProjectName("logback-mdc-monix3"), crossProject(JVMPlatform))
    .settings(
      description := "Logger for F[_] - logback MDC context map support for Monix 3",
      libraryDependencies ++= Seq(
        libs.logbackClassic,
        libs.logbackScalaInterop,
        libs.monix3Execution.value,
        libs.slf4jApi % Test,
        libs.tests.monix.value,
        libs.tests.effectieMonix3.value,
      ) ++ libs.tests.hedgehogLibs.value,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(
      core,
      slf4jMdc,
      monix       % Test,
      slf4jLogger % Test,
    )
lazy val logbackMdcMonix3Jvm = logbackMdcMonix3.jvm

lazy val testLogbackMdcMonix3    = testProject(ProjectName("logback-mdc-monix3"), crossProject(JVMPlatform))
  .settings(
    description := "Logger for F[_] - testing logback MDC context map support for Monix 3",
    libraryDependencies ++= Seq(
      libs.slf4jApiLatest            % Test,
      libs.logbackClassicLatest      % Test,
      libs.logbackScalaInteropLatest % Test,
      libs.monix3Execution.value     % Test,
      libs.tests.monix.value,
      libs.tests.effectieMonix3.value,
    ) ++ libs.tests.hedgehogLibs.value,
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
  )
  .dependsOn(
    core             % Test,
    slf4jMdc         % Test,
    logbackMdcMonix3 % "test->test",
    monix            % Test,
    slf4jLogger      % Test,
  )
lazy val testLogbackMdcMonix3Jvm = testLogbackMdcMonix3.jvm

lazy val logbackMdcCatsEffect3    = module(ProjectName("logback-mdc-cats-effect3"), crossProject(JVMPlatform))
  .settings(
    description := "Logger for F[_] - logback MDC context map support for Cats Effect 3",
    libraryDependencies ++= Seq(
      libs.logbackClassic,
      libs.logbackScalaInterop,
      libs.catsEffect3Eap,
      libs.tests.effectieCatsEffect3.value,
      libs.tests.extrasHedgehogCatsEffect3.value,
    ) ++ libs.tests.hedgehogLibs.value,
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
    javaOptions += "-Dcats.effect.ioLocalPropagation=true",
  )
  .dependsOn(
    core,
    monix       % Test,
    slf4jLogger % Test,
  )
lazy val logbackMdcCatsEffect3Jvm = logbackMdcCatsEffect3.jvm

lazy val doobie1    = module(ProjectName("doobie1"), crossProject(JVMPlatform))
  .settings(
    description := "Logger for F[_] - for Doobie v1",
    libraryDependencies ++= Seq(
      libs.doobieFree,
      libs.tests.effectieCatsEffect3.value,
      libs.tests.extrasHedgehogCatsEffect3.value,
    ) ++ libs.tests.hedgehogLibs.value,
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
  )
  .dependsOn(
    core,
    cats,
    testKit     % Test,
    slf4jLogger % Test,
  )
lazy val doobie1Jvm = doobie1.jvm

lazy val testKit       =
  module(ProjectName("test-kit"), crossProject(JVMPlatform, JSPlatform, NativePlatform))
    .settings(
      description := "Logger for F[_] - Test Kit",
      libraryDependencies ++= libs.tests.hedgehogLibs.value ++
//        libs.tests.hedgehogExtra.value ++
        List(
          libs.cats.value
        ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core % props.IncludeTest)
lazy val testKitJvm    = testKit.jvm
lazy val testKitJs     = testKit
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )
lazy val testKitNative = testKit.native.settings(nativeSettings)

lazy val catsEffect    =
  module(ProjectName("cats-effect"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Cats Effect",
      libraryDependencies ++= libs.tests.hedgehogLibs.value ++ List(libs.effectieCatsEffect2.value % Test),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val catsEffectJvm = catsEffect.jvm
lazy val catsEffectJs  = catsEffect
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )

lazy val catsEffect3       =
  module(ProjectName("cats-effect3"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Cats Effect 3",
      libraryDependencies ++= libs.tests.hedgehogLibs.value ++ List(
        libs.effectieCatsEffect3.value % Test,
        libs.tests.extrasHedgehogCatsEffect3.value,
      ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val catsEffect3Jvm    = catsEffect3.jvm
lazy val catsEffect3Js     = catsEffect3
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )

lazy val monix    =
  module(ProjectName("monix"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Monix",
      libraryDependencies ++= libs.tests.hedgehogLibs.value ++ List(libs.effectieMonix.value % Test),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val monixJvm = monix.jvm
lazy val monixJs  = monix
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )

lazy val testCore    =
  testProject(
    ProjectName("core"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Core module",
      libraryDependencies ++= Seq(
        libs.slf4jApi,
        libs.logbackClassic,
        libs.tests.extrasTestingTools.value,
      ) ++ libs.tests.hedgehogLibs.value,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core, log4sLogger)
lazy val testCoreJvm = testCore.jvm
lazy val testCoreJs  = testCore
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )

lazy val testCoreWithCats    =
  testProject(
    ProjectName("core-with-cats"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Core module",
      libraryDependencies ++= Seq(
        libs.slf4jApi,
        libs.logbackClassic,
        libs.cats.value,
      ) ++ libs.tests.hedgehogLibs.value,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core, testCore % props.IncludeTest)
lazy val testCoreWithCatsJvm = testCoreWithCats.jvm
lazy val testCoreWithCatsJs  = testCoreWithCats
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )

lazy val testCatsEffectWithSlf4jLogger    =
  testProject(
    ProjectName("cats-effect-slf4j"),
    crossProject(JVMPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Slf4j",
      libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic, libs.tests.extrasCats.value),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, slf4jLogger, catsEffect % props.IncludeTest)
lazy val testCatsEffectWithSlf4jLoggerJvm = testCatsEffectWithSlf4jLogger.jvm

lazy val testMonixWithSlf4jLogger    =
  testProject(
    ProjectName("monix-slf4j"),
    crossProject(JVMPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Slf4j",
      libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic, libs.tests.extrasCats.value),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, slf4jLogger, monix % props.IncludeTest)
lazy val testMonixWithSlf4jLoggerJvm = testMonixWithSlf4jLogger.jvm

lazy val testCatsEffectWithLog4sLogger    =
  testProject(
    ProjectName("cats-effect-log4s"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Log4s",
      libraryDependencies ++= Seq(libs.log4sLib.value, libs.logbackClassic, libs.tests.extrasCats.value),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, log4sLogger, catsEffect3 % props.IncludeTest)
lazy val testCatsEffectWithLog4sLoggerJvm = testCatsEffectWithLog4sLogger.jvm
lazy val testCatsEffectWithLog4sLoggerJs  = testCatsEffectWithLog4sLogger
  .js
  .settings(
    jsSettings,
    jsSettingsForFuture,
  )

lazy val testCatsEffectWithLog4jLogger    =
  testProject(
    ProjectName("cats-effect-log4j"),
    crossProject(JVMPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Log4j",
      libraryDependencies ++= Seq(libs.log4jApi, libs.log4jCore, libs.tests.extrasCats.value),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, log4jLogger, catsEffect % props.IncludeTest)
lazy val testCatsEffectWithLog4jLoggerJvm = testCatsEffectWithLog4jLogger.jvm


lazy val props =
  new {

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "logger-f"

    final val Scala3Versions = List("3.3.5")
    final val Scala2Versions = List("2.13.16", "2.12.18")

//    final val ProjectScalaVersion = Scala3Versions.head
    final val ProjectScalaVersion = Scala2Versions.head

    lazy val licenses = List("MIT" -> url("http://opensource.org/licenses/MIT"))

    val removeDottyIncompatible: ModuleID => Boolean =
      m =>
        m.name == "ammonite" ||
          m.name == "kind-projector" ||
          m.name == "better-monadic-for" ||
          m.name == "mdoc"

    val CrossScalaVersions = (Scala3Versions ++ Scala2Versions).distinct

    val CrossScalaVersionsForScalaJsAndNative = CrossScalaVersions.filterNot(_.startsWith("2.12"))

    final val IncludeTest = "compile->compile;test->test"

    val HedgehogVersion = "0.13.0"

    val HedgehogExtraVersion = "0.15.0"

    val EffectieVersion = "2.3.0"

    final val CatsVersion = "2.12.0"

    val catsEffect3Version          = "3.3.14"
    val catsEffect3ForNativeVersion = "3.7.0-RC1"

    val Monix3Version = "3.4.0"

    val Doobie1Version = "1.0.0-RC10"

    final val LoggerF1Version = "1.20.0"

    final val ExtrasVersion = "0.49.0"

    val Slf4JVersion       = "2.0.12"
    val Slf4JLatestVersion = "2.0.17"

    val LogbackVersion       = "1.5.0"
    val LogbackLatestVersion = "1.5.18"

    final val Log4sVersion = "1.10.0"

    final val Log4JVersion = "2.19.0"

    val LogbackScalaInteropVersion       = "1.0.0"
    val LogbackScalaInteropLatestVersion = "1.17.0"

    val OrphanVersion = "0.5.0"

    val MunitVersion = "0.7.29"

    val MunitCatsEffectVersion = "1.0.7"

    val ScalaJsMacrotaskExecutorVersion = "1.1.1"

    val ScalaJavaTimeVersion = "2.6.0"

  }

lazy val libs =
  new {

    lazy val slf4jApi: ModuleID       = "org.slf4j" % "slf4j-api" % props.Slf4JVersion
    lazy val slf4jApiLatest: ModuleID = "org.slf4j" % "slf4j-api" % props.Slf4JLatestVersion

    lazy val logbackClassic: ModuleID       = "ch.qos.logback" % "logback-classic" % props.LogbackVersion
    lazy val logbackClassicLatest: ModuleID = "ch.qos.logback" % "logback-classic" % props.LogbackLatestVersion

    lazy val log4sLib = Def.setting("org.log4s" %%% "log4s" % props.Log4sVersion)

    lazy val log4jApi  = "org.apache.logging.log4j" % "log4j-api"  % props.Log4JVersion
    lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % props.Log4JVersion

    def sbtLoggingLib(sbtLoggingVersion: String) = "org.scala-sbt" %% "util-logging" % sbtLoggingVersion

    lazy val cats = Def.setting("org.typelevel" %%% "cats-core" % props.CatsVersion)

    def libCatsEffect(catsEffectVersion: String) = Def.setting("org.typelevel" %%% "cats-effect" % catsEffectVersion)

    lazy val catsEffect3Eap = "org.typelevel" %% "cats-effect" % "3.6-02a43a6"

    lazy val monix3Execution = Def.setting("io.monix" %%% "monix-execution" % props.Monix3Version)

    lazy val effectieCore        = Def.setting("io.kevinlee" %%% "effectie-core" % props.EffectieVersion)
    lazy val effectieSyntax      = Def.setting("io.kevinlee" %%% "effectie-syntax" % props.EffectieVersion)
    lazy val effectieCats        = Def.setting("io.kevinlee" %%% "effectie-cats" % props.EffectieVersion)
    lazy val effectieCatsEffect2 = Def.setting("io.kevinlee" %%% "effectie-cats-effect2" % props.EffectieVersion)
    lazy val effectieCatsEffect3 = Def.setting("io.kevinlee" %%% "effectie-cats-effect3" % props.EffectieVersion)

    lazy val effectieMonix = Def.setting("io.kevinlee" %%% "effectie-monix3" % props.EffectieVersion)

    lazy val logbackScalaInterop       = "io.kevinlee" % "logback-scala-interop" % props.LogbackScalaInteropVersion
    lazy val logbackScalaInteropLatest =
      "io.kevinlee" % "logback-scala-interop" % props.LogbackScalaInteropLatestVersion

    lazy val orphanCats = Def.setting("io.kevinlee" %%% "orphan-cats" % props.OrphanVersion)

    lazy val doobieFree = "org.tpolecat" %% "doobie-free" % props.Doobie1Version

    lazy val tests = new {

      lazy val monix = Def.setting("io.monix" %%% "monix" % props.Monix3Version % Test)

      lazy val effectieCatsEffect3 =
        Def.setting("io.kevinlee" %%% "effectie-cats-effect3" % props.EffectieVersion % Test)

      lazy val effectieMonix3 = Def.setting("io.kevinlee" %%% "effectie-monix3" % props.EffectieVersion % Test)

      lazy val hedgehogLibs = Def.setting(
        List(
          "qa.hedgehog" %%% "hedgehog-core"   % props.HedgehogVersion % Test,
          "qa.hedgehog" %%% "hedgehog-runner" % props.HedgehogVersion % Test,
          "qa.hedgehog" %%% "hedgehog-sbt"    % props.HedgehogVersion % Test,
        )
      )

      lazy val hedgehogExtra = Def.setting(
        List(
          "io.kevinlee" %%% "hedgehog-extra-core" % props.HedgehogExtraVersion
        ).map(_ % Test)
      )

      lazy val extrasCats = Def.setting("io.kevinlee" %%% "extras-cats" % props.ExtrasVersion % Test)

      lazy val extrasTestingTools = Def.setting("io.kevinlee" %%% "extras-testing-tools" % props.ExtrasVersion % Test)

      lazy val extrasHedgehogCatsEffect3 =
        Def.setting("io.kevinlee" %%% "extras-hedgehog-ce3" % props.ExtrasVersion % Test)

      lazy val extrasConcurrent        = Def.setting("io.kevinlee" %%% "extras-concurrent" % props.ExtrasVersion % Test)
      lazy val extrasConcurrentTesting = "io.kevinlee" %% "extras-concurrent-testing" % props.ExtrasVersion % Test

      lazy val scalaJsMacrotaskExecutor =
        Def.setting("org.scala-js" %%% "scala-js-macrotask-executor" % props.ScalaJsMacrotaskExecutorVersion % Test)

      lazy val munit = Def.setting("org.scalameta" %%% "munit" % props.MunitVersion % Test)

      lazy val munitCatsEffect3 =
        Def.setting("org.typelevel" %%% "munit-cats-effect-3" % props.MunitCatsEffectVersion % Test)
    }

  }

// scalafmt: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def libraryDependenciesRemoveScala3Incompatible(
  scalaVersion: String,
  libraries: Seq[ModuleID],
): Seq[ModuleID] =
  (
    if (scalaVersion.startsWith("3."))
      libraries
        .filterNot(props.removeDottyIncompatible)
    else
      libraries
  )

def module(projectName: ProjectName, crossProject: CrossProject.Builder): CrossProject = {
  val prefixedName = prefixedProjectName(projectName.projectName)
  projectCommonSettings(prefixedName, crossProject)
}

def testProject(projectName: ProjectName, crossProject: CrossProject.Builder): CrossProject = {
  val prefixedName = s"test-${prefixedProjectName(projectName.projectName)}"
  projectCommonSettings(prefixedName, crossProject)
    .settings(
      // Disable publishing tasks
      publish / skip := true,
      publish := {},
      publishLocal := {},
      // Prevent artifact generation for publishing
      publishArtifact := false,
      packagedArtifacts := Map.empty,
      // Disable specific packaging tasks
      packageBin / publishArtifact := false,
      packageDoc / publishArtifact := false,
      packageSrc / publishArtifact := false,
    )
}

def projectCommonSettings(projectName: String, crossProject: CrossProject.Builder): CrossProject =
  crossProject
    .in(file(s"modules/$projectName"))
    .settings(
      name := projectName,
      licenses := props.licenses,
      scalafixConfig := (
        if (scalaVersion.value.startsWith("3"))
          ((ThisBuild / baseDirectory).value / ".scalafix-scala3.conf").some
        else
          ((ThisBuild / baseDirectory).value / ".scalafix-scala2.conf").some
      ),
      /* WartRemover and scalacOptions { */
      //      , Compile / compile / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value)
      //      , Test / compile / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value)
      wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value),
      Compile / console / wartremoverErrors := List.empty,
      Compile / console / wartremoverWarnings := List.empty,
      Compile / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / wartremoverErrors := List.empty,
      Test / console / wartremoverWarnings := List.empty,
      Test / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      /* } WartRemover and scalacOptions */
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
      /* Coveralls { */
      coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) | Some((2, 11)) =>
          false
        case _ =>
          true
      }),
      /* } Coveralls */
    )

lazy val jsSettingsForFuture: SettingsDefinition = List(
  libraryDependencies ++= List(
    libs.tests.scalaJsMacrotaskExecutor.value,
    libs.tests.munit.value,
  )
)
//lazy val jsSettingsForFuture: SettingsDefinition = List(
//  Test / scalacOptions ++= (if (scalaVersion.value.startsWith("3")) List.empty
//                            else List("-P:scalajs:nowarnGlobalExecutionContext")),
//  Test / compile / scalacOptions ++= (if (scalaVersion.value.startsWith("3")) List.empty
//                                      else List("-P:scalajs:nowarnGlobalExecutionContext")),
//)

lazy val jsSettings: SettingsDefinition = List(
  crossScalaVersions := props.CrossScalaVersionsForScalaJsAndNative,
  Test / fork := false,
  coverageEnabled := false,
)

lazy val nativeSettings: SettingsDefinition = List(
  crossScalaVersions := props.CrossScalaVersionsForScalaJsAndNative,
  Test / fork := false,
  coverageEnabled := false,
)
