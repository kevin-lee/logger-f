import ProjectInfo.{ProjectName, _}
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
//    coreJs,
    slf4jLoggerJvm,
//    slf4jLoggerJs,
    log4sLoggerJvm,
//    log4sLoggerJs,
    log4jLoggerJvm,
//    log4jLoggerJs,
    sbtLoggingJvm,
//    sbtLoggingJs,
    catsJvm,
//    catsJs,
    slf4jMdcJvm,
    logbackMdcMonix3Jvm,
    testLogbackMdcMonix3Jvm,
    testKitJvm,
//    testKitJs,
    catsEffectJvm,
//    catsEffectJs,
    catsEffect3Jvm,
//    catsEffect3Js,
    monixJvm,
//    monixJs,
  )

lazy val core    =
  module(ProjectName("core"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Core",
      libraryDependencies ++= List(
        libs.effectieCore,
        libs.cats % Test,
        libs.tests.extrasConcurrent,
        libs.tests.extrasConcurrentTesting,
      ) ++ libs.tests.hedgehogLibs,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

lazy val slf4jLogger    = module(ProjectName("slf4j"), crossProject(JVMPlatform, JSPlatform))
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
lazy val slf4jLoggerJvm = slf4jLogger.jvm
lazy val slf4jLoggerJs  = slf4jLogger.js

lazy val log4sLogger    =
  module(ProjectName("log4s"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Logger with Log4s",
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
      libraryDependencies ++= List(
        libs.log4sLib
      ),
    )
    .dependsOn(core)
lazy val log4sLoggerJvm = log4sLogger.jvm
lazy val log4sLoggerJs  = log4sLogger.js

lazy val log4jLogger    =
  module(ProjectName("log4j"), crossProject(JVMPlatform, JSPlatform))
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
lazy val log4jLoggerJvm = log4jLogger.jvm
lazy val log4jLoggerJs  = log4jLogger.js

lazy val sbtLogging    =
  module(ProjectName("sbt-logging"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Logger with sbt logging",
      libraryDependencies ++= crossVersionProps(
        List.empty,
        SemVer.parseUnsafe(scalaVersion.value),
      ) {
        case (SemVer.Major(2), SemVer.Minor(11), _) =>
          List(
            libs.sbtLoggingLib % "1.2.4"
          )

        case (SemVer.Major(2), SemVer.Minor(12), _) =>
          List(
            libs.sbtLoggingLib % "1.3.3"
          )

        case (SemVer.Major(2), SemVer.Minor(13), _) | (SemVer.Major(3), SemVer.Minor(_), _) =>
          List(
            libs.sbtLoggingLib % "1.5.8"
          ).map(_.cross(CrossVersion.for3Use2_13))
      },
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core)
lazy val sbtLoggingJvm = sbtLogging.jvm
lazy val sbtLoggingJs  = sbtLogging.js

lazy val cats    =
  module(ProjectName("cats"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Cats",
      libraryDependencies ++= libs.tests.hedgehogLibs ++ List(
        libs.effectieCore,
        libs.cats,
        libs.effectieSyntax,
        libs.effectieCats % Test,
      ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core % props.IncludeTest)
lazy val catsJvm = cats.jvm
lazy val catsJs  = cats.js

lazy val slf4jMdc    = module(ProjectName("slf4j-mdc"), crossProject(JVMPlatform))
  .settings(
    description := "Logger for F[_] - A tool to set MDC's MDCAdapter",
    libraryDependencies ++= Seq(
      libs.slf4jApi,
      libs.cats % Test,
    ) ++ libs.tests.hedgehogLibs,
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
  )
  .dependsOn(
    core
  )
lazy val slf4jMdcJvm = slf4jMdc.jvm

lazy val logbackMdcMonix3    = module(ProjectName("logback-mdc-monix3"), crossProject(JVMPlatform))
  .settings(
    description := "Logger for F[_] - logback MDC context map support for Monix 3",
    libraryDependencies ++= Seq(
      libs.logbackClassic,
      libs.logbackScalaInterop,
      libs.monix3Execution,
      libs.slf4jApi % Test,
      libs.tests.monix,
      libs.tests.effectieMonix3,
    ) ++ libs.tests.hedgehogLibs,
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
      libs.monix3Execution           % Test,
      libs.tests.monix,
      libs.tests.effectieMonix3,
    ) ++ libs.tests.hedgehogLibs,
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

lazy val testKit    =
  module(ProjectName("test-kit"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Test Kit",
      libraryDependencies ++= libs.tests.hedgehogLibs ++
//        libs.tests.hedgehogExtra ++
        List(
          libs.cats
        ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .dependsOn(core % props.IncludeTest)
lazy val testKitJvm = testKit.jvm
lazy val testKitJs  = testKit.js

lazy val catsEffect    =
  module(ProjectName("cats-effect"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Cats Effect",
      libraryDependencies ++= libs.tests.hedgehogLibs ++ List(libs.effectieCatsEffect2 % Test),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val catsEffectJvm = catsEffect.jvm
lazy val catsEffectJs  = catsEffect.js

lazy val catsEffect3    =
  module(ProjectName("cats-effect3"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Cats Effect 3",
      libraryDependencies ++= libs.tests.hedgehogLibs ++ List(
        libs.effectieCatsEffect3 % Test,
        libs.tests.extrasHedgehogCatsEffect3,
      ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val catsEffect3Jvm = catsEffect3.jvm
lazy val catsEffect3Js  = catsEffect3.js

lazy val monix    =
  module(ProjectName("monix"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description := "Logger for F[_] - Monix",
      libraryDependencies ++= libs.tests.hedgehogLibs ++ List(libs.effectieMonix % Test),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val monixJvm = monix.jvm
lazy val monixJs  = monix.js

lazy val testCatsEffectWithSlf4jLogger    =
  testProject(
    ProjectName("cats-effect-slf4j"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Slf4j",
      libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic, libs.tests.extrasCats),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, slf4jLogger, catsEffect % props.IncludeTest)
lazy val testCatsEffectWithSlf4jLoggerJvm = testCatsEffectWithSlf4jLogger.jvm
lazy val testCatsEffectWithSlf4jLoggerJs  = testCatsEffectWithSlf4jLogger.js

lazy val testMonixWithSlf4jLogger    =
  testProject(
    ProjectName("monix-slf4j"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Slf4j",
      libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic, libs.tests.extrasCats),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, slf4jLogger, monix % props.IncludeTest)
lazy val testMonixWithSlf4jLoggerJvm = testMonixWithSlf4jLogger.jvm
lazy val testMonixWithSlf4jLoggerJs  = testMonixWithSlf4jLogger.js

lazy val testCatsEffectWithLog4sLogger    =
  testProject(
    ProjectName("cats-effect-log4s"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Log4s",
      libraryDependencies ++= Seq(libs.log4sLib, libs.logbackClassic, libs.tests.extrasCats),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, log4sLogger, catsEffect % props.IncludeTest)
lazy val testCatsEffectWithLog4sLoggerJvm = testCatsEffectWithLog4sLogger.jvm
lazy val testCatsEffectWithLog4sLoggerJs  = testCatsEffectWithLog4sLogger.js

lazy val testCatsEffectWithLog4jLogger    =
  testProject(
    ProjectName("cats-effect-log4j"),
    crossProject(JVMPlatform, JSPlatform),
  )
    .settings(
      description := "Test Logger for F[_] - Logger with Log4j",
      libraryDependencies ++= Seq(libs.log4jApi, libs.log4jCore, libs.tests.extrasCats),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value,
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, log4jLogger, catsEffect % props.IncludeTest)
lazy val testCatsEffectWithLog4jLoggerJvm = testCatsEffectWithLog4jLogger.jvm
lazy val testCatsEffectWithLog4jLoggerJs  = testCatsEffectWithLog4jLogger.js

lazy val docs = (project in file("docs-gen-tmp/docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
    name := "docs",
    mdocIn := file("docs/latest"),
    mdocOut := file("generated-docs/docs"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs"),
    scalacOptions ~= (_.filter(opt => opt != "-Xfatal-warnings")),
    libraryDependencies ++= {
      val latestTag = getTheLatestTaggedVersion()
      Seq(
        libs.effectieCore,
        libs.effectieSyntax,
        libs.effectieCatsEffect2,
        "io.kevinlee" %% "logger-f-core"        % latestTag,
        "io.kevinlee" %% "logger-f-cats"        % latestTag,
        "io.kevinlee" %% "logger-f-slf4j"       % latestTag,
        "io.kevinlee" %% "logger-f-log4j"       % latestTag,
        "io.kevinlee" %% "logger-f-log4s"       % latestTag,
        "io.kevinlee" %% "logger-f-sbt-logging" % latestTag,
        libs.slf4jApi,
        libs.logbackClassic,
      )
    },
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
    mdocVariables := createMdocVariables(none),
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
  )
  .settings(noPublish)

lazy val docsV1 = (project in file("docs-gen-tmp/docs-v1"))
  .enablePlugins(MdocPlugin)
  .settings(
    name := "docsV1",
    mdocIn := file("docs/v1"),
    mdocOut := file("website/versioned_docs/version-v1/docs"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "website" / "versioned_docs" / "version-v1"),
    scalacOptions ~= (_.filter(opt => opt != "-Xfatal-warnings")),
    libraryDependencies ++=
      Seq(
        "io.kevinlee" %% "logger-f-cats-effect"   % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-monix"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-scalaz-effect" % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-slf4j"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-log4j"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-log4s"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-sbt-logging"   % props.LoggerF1Version,
        libs.slf4jApi,
        libs.logbackClassic,
      ),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    ),
    mdocVariables := createMdocVariables(props.LoggerF1Version.some),
  )
  .settings(noPublish)

addCommandAlias(
  "docsCleanAll",
  "; docs/clean; docsV1/clean",
)
addCommandAlias(
  "docsMdocAll",
  "; docs/mdoc; docsV1/mdoc",
)

def getTheLatestTaggedVersion(): String                               = {
  import sys.process._
  "git fetch --tags".!
  val tag = "git rev-list --tags --max-count=1".!!.trim
  s"git describe --tags $tag".!!.trim.stripPrefix("v")
}
def createMdocVariables(version: Option[String]): Map[String, String] = Map(
  "VERSION"                  -> (version match {
    case Some(version) => version
    case None => getTheLatestTaggedVersion()
  }),
  "SUPPORTED_SCALA_VERSIONS" -> {
    val versions = props
      .CrossScalaVersions
      .map(CrossVersion.binaryScalaVersion)
      .map(binVer => s"`$binVer`")
    if (versions.length > 1)
      s"${versions.init.mkString(", ")} and ${versions.last}"
    else
      versions.mkString
  },
)

lazy val props =
  new {

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "logger-f"

    final val Scala3Versions = List("3.1.3")
    final val Scala2Versions = List("2.13.14", "2.12.18")

//    final val ProjectScalaVersion = Scala3Versions.head
    final val ProjectScalaVersion = Scala2Versions.head

    lazy val licenses = List("MIT" -> url("http://opensource.org/licenses/MIT"))

    val removeDottyIncompatible: ModuleID => Boolean =
      m =>
          m.name == "ammonite" ||
          m.name == "kind-projector" ||
          m.name == "better-monadic-for" ||
          m.name == "mdoc"

    final val CrossScalaVersions =
      (Scala3Versions ++ Scala2Versions).distinct

    final val IncludeTest = "compile->compile;test->test"

    final val HedgehogVersion = "0.10.1"

    final val HedgehogExtraVersion = "0.10.0"

    final val EffectieVersion = "2.0.0"

    final val CatsVersion = "2.7.0"

    val CatsEffect3Version = "3.3.14"

    val Monix3Version = "3.4.0"

    final val LoggerF1Version = "1.20.0"

    final val ExtrasVersion = "0.25.0"

    val Slf4JVersion       = "2.0.12"
    val Slf4JLatestVersion = "2.0.17"

    val LogbackVersion       = "1.5.0"
    val LogbackLatestVersion = "1.5.17"

    final val Log4sVersion = "1.10.0"

    final val Log4JVersion = "2.19.0"

    val LogbackScalaInteropVersion       = "1.0.0"
    val LogbackScalaInteropLatestVersion = "1.17.0"
  }

lazy val libs =
  new {

    lazy val slf4jApi: ModuleID       = "org.slf4j" % "slf4j-api" % props.Slf4JVersion
    lazy val slf4jApiLatest: ModuleID = "org.slf4j" % "slf4j-api" % props.Slf4JLatestVersion

    lazy val logbackClassic: ModuleID       = "ch.qos.logback" % "logback-classic" % props.LogbackVersion
    lazy val logbackClassicLatest: ModuleID = "ch.qos.logback" % "logback-classic" % props.LogbackLatestVersion

    lazy val log4sLib: ModuleID = "org.log4s" %% "log4s" % props.Log4sVersion

    lazy val log4jApi  = "org.apache.logging.log4j" % "log4j-api"  % props.Log4JVersion
    lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % props.Log4JVersion

    lazy val sbtLoggingLib = "org.scala-sbt" %% "util-logging"

    lazy val cats = "org.typelevel" %% "cats-core" % props.CatsVersion

    lazy val catsEffect3 = "org.typelevel" %% "cats-effect" % props.CatsEffect3Version

    lazy val monix3Execution = "io.monix" %% "monix-execution" % props.Monix3Version

    lazy val effectieCore: ModuleID        = "io.kevinlee" %% "effectie-core"         % props.EffectieVersion
    lazy val effectieSyntax: ModuleID      = "io.kevinlee" %% "effectie-syntax"       % props.EffectieVersion
    lazy val effectieCats: ModuleID        = "io.kevinlee" %% "effectie-cats"         % props.EffectieVersion
    lazy val effectieCatsEffect2: ModuleID = "io.kevinlee" %% "effectie-cats-effect2" % props.EffectieVersion
    lazy val effectieCatsEffect3: ModuleID = "io.kevinlee" %% "effectie-cats-effect3" % props.EffectieVersion

    lazy val effectieMonix: ModuleID = "io.kevinlee" %% "effectie-monix3" % props.EffectieVersion

    lazy val logbackScalaInterop       = "io.kevinlee" % "logback-scala-interop" % props.LogbackScalaInteropVersion
    lazy val logbackScalaInteropLatest =
      "io.kevinlee" % "logback-scala-interop" % props.LogbackScalaInteropLatestVersion

    lazy val tests = new {

      lazy val monix = "io.monix" %% "monix" % props.Monix3Version % Test

      lazy val effectieMonix3 = "io.kevinlee" %% "effectie-monix3" % props.EffectieVersion % Test

      lazy val hedgehogLibs: List[ModuleID] = List(
        "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion,
        "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion,
        "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion,
      ).map(_ % Test)

      lazy val hedgehogExtra = List(
        "io.kevinlee" %% "hedgehog-extra-core" % props.HedgehogExtraVersion
      ).map(_ % Test)

      lazy val extrasCats = "io.kevinlee" %% "extras-cats" % props.ExtrasVersion % Test

      lazy val extrasConcurrent        = "io.kevinlee" %% "extras-concurrent"         % props.ExtrasVersion % Test
      lazy val extrasConcurrentTesting = "io.kevinlee" %% "extras-concurrent-testing" % props.ExtrasVersion % Test

      lazy val extrasHedgehogCatsEffect3 = "io.kevinlee" %% "extras-hedgehog-ce3" % props.ExtrasVersion % Test
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
