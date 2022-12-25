import ProjectInfo.{ProjectName, _}
import just.semver.SemVer
import kevinlee.sbt.SbtCommon.crossVersionProps
import sbtcrossproject.CrossProject

ThisBuild / scalaVersion       := props.ProjectScalaVersion
ThisBuild / organization       := "io.kevinlee"
ThisBuild / organizationName   := "Kevin's Code"
ThisBuild / crossScalaVersions := props.CrossScalaVersions

ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}")
  )
)
ThisBuild / homepage   := url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}").some
ThisBuild / scmInfo    :=
  ScmInfo(
    browseUrl = url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
    connection = s"scm:git:git@github.com:${props.GitHubUsername}/${props.RepoName}.git"
  ).some

ThisBuild / licenses   := props.licenses

ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"

lazy val loggerF = (project in file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin)
  .settings(
    name                     := prefixedProjectName(""),
    description              := "Logger for F[_]",
    libraryDependencies      := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value,
    )
    /* GitHub Release { */,
    devOopsPackagedArtifacts := List(s"*/*/*/target/scala-*/${devOopsArtifactNamePrefix.value}*.jar"),
    /* } GitHub Release */
  )
  .settings(mavenCentralPublishSettings)
  .settings(noPublish)
  .aggregate(
    coreJvm,
    coreJs,
    slf4jLoggerJvm,
    slf4jLoggerJs,
    log4sLoggerJvm,
    log4sLoggerJs,
    log4jLoggerJvm,
    log4jLoggerJs,
    sbtLoggingJvm,
    sbtLoggingJs,
    catsJvm,
    catsJs,
    catsEffectJvm,
    catsEffectJs,
    catsEffect3Jvm,
    catsEffect3Js,
    monixJvm,
    monixJs,
  )

lazy val core    =
  module(ProjectName("core"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Core",
      libraryDependencies ++= List(
        libs.effectieCore,
        libs.cats % Test,
        libs.extrasConcurrent,
        libs.extrasConcurrentTesting,
      ) ++ libs.hedgehogLibs,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
    )
lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

lazy val slf4jLogger    = module(ProjectName("slf4j"), crossProject(JVMPlatform, JSPlatform))
  .settings(
    description         := "Logger for F[_] - Logger with Slf4j",
    libraryDependencies ++= Seq(
      libs.slf4jApi % Provided
    ),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .dependsOn(core)
lazy val slf4jLoggerJvm = slf4jLogger.jvm
lazy val slf4jLoggerJs  = slf4jLogger.js

lazy val log4sLogger    =
  module(ProjectName("log4s"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Logger with Log4s",
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
      libraryDependencies ++= List(
        libs.log4sLib % Provided
      )
    )
    .dependsOn(core)
lazy val log4sLoggerJvm = log4sLogger.jvm
lazy val log4sLoggerJs  = log4sLogger.js

lazy val log4jLogger    =
  module(ProjectName("log4j"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Logger with Log4j",
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
        libs.log4jCore
      ).map(_ % Provided),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
    )
    .dependsOn(core)
lazy val log4jLoggerJvm = log4jLogger.jvm
lazy val log4jLoggerJs  = log4jLogger.js

lazy val sbtLogging    =
  module(ProjectName("sbt-logging"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Logger with sbt logging",
      libraryDependencies ++= crossVersionProps(
        List.empty,
        SemVer.parseUnsafe(scalaVersion.value)
      ) {
        case (SemVer.Major(2), SemVer.Minor(11), _) =>
          List(
            libs.sbtLoggingLib % "1.2.4"
          ).map(_ % Provided)

        case (SemVer.Major(2), SemVer.Minor(12), _) =>
          List(
            libs.sbtLoggingLib % "1.3.3"
          ).map(_ % Provided)

        case (SemVer.Major(2), SemVer.Minor(13), _) | (SemVer.Major(3), SemVer.Minor(0), _) =>
          List(
            libs.sbtLoggingLib % "1.5.8"
          ).map(_ % Provided).map(_.cross(CrossVersion.for3Use2_13))
      },
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
    )
    .dependsOn(core)
lazy val sbtLoggingJvm = sbtLogging.jvm
lazy val sbtLoggingJs  = sbtLogging.js

lazy val cats    =
  module(ProjectName("cats"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Cats",
      libraryDependencies ++= libs.hedgehogLibs ++ List(
        libs.effectieCore,
        libs.cats,
        libs.extrasCats,
        libs.effectieSyntax,
      ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
    )
    .dependsOn(core % props.IncludeTest)
lazy val catsJvm = cats.jvm
lazy val catsJs  = cats.js

lazy val catsEffect    =
  module(ProjectName("cats-effect"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Cats Effect",
      libraryDependencies ++= libs.hedgehogLibs ++ List(libs.effectieCatsEffect % Test),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val catsEffectJvm = catsEffect.jvm
lazy val catsEffectJs  = catsEffect.js

lazy val catsEffect3    =
  module(ProjectName("cats-effect3"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Cats Effect 3",
      libraryDependencies ++= libs.hedgehogLibs ++ List(
        libs.effectieCatsEffect3 % Test,
        libs.extrasHedgehogCatsEffect3
      ),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, cats)
lazy val catsEffect3Jvm = catsEffect3.jvm
lazy val catsEffect3Js  = catsEffect3.js

lazy val monix    =
  module(ProjectName("monix"), crossProject(JVMPlatform, JSPlatform))
    .settings(
      description         := "Logger for F[_] - Monix",
      libraryDependencies ++= libs.hedgehogLibs ++ List(libs.effectieMonix % Test),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
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
      description         := "Test Logger for F[_] - Logger with Slf4j",
      libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
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
      description         := "Test Logger for F[_] - Logger with Slf4j",
      libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
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
      description         := "Test Logger for F[_] - Logger with Log4s",
      libraryDependencies ++= Seq(libs.log4sLib, libs.logbackClassic),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
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
      description         := "Test Logger for F[_] - Logger with Log4j",
      libraryDependencies ++= Seq(libs.log4jApi, libs.log4jCore),
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core % props.IncludeTest, log4jLogger, catsEffect % props.IncludeTest)
lazy val testCatsEffectWithLog4jLoggerJvm = testCatsEffectWithLog4jLogger.jvm
lazy val testCatsEffectWithLog4jLoggerJs  = testCatsEffectWithLog4jLogger.js

lazy val docs = (project in file("generated-docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
    name                := prefixedProjectName("docs"),
    libraryDependencies ++=
      Seq(
        "io.kevinlee" %% "logger-f-cats-effect"   % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-monix"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-scalaz-effect" % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-slf4j"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-log4j"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-log4s"         % props.LoggerF1Version,
        "io.kevinlee" %% "logger-f-sbt-logging"   % props.LoggerF1Version,
      ),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    ),
    mdocVariables       := Map(
//      "VERSION"                  -> {
//        import sys.process._
//        "git fetch --tags".!
//        val tag = "git rev-list --tags --max-count=1".!!.trim
//        s"git describe --tags $tag".!!.trim.stripPrefix("v")
//      },
      "VERSION"                  -> props.LoggerF1Version,
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
    ),
    docusaurDir         := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir    := docusaurDir.value / "build",
  )
  .settings(noPublish)

lazy val props =
  new {

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "logger-f"

    final val Scala3Versions = List("3.0.2")
    final val Scala2Versions = List("2.13.6", "2.12.13")

//    final val ProjectScalaVersion = Scala3Versions.head
    final val ProjectScalaVersion = Scala2Versions.head

    lazy val licenses = List("MIT" -> url("http://opensource.org/licenses/MIT"))

    val SonatypeCredentialHost = "s01.oss.sonatype.org"
    val SonatypeRepository     = s"https://$SonatypeCredentialHost/service/local"

    val removeDottyIncompatible: ModuleID => Boolean =
      m =>
        m.name == "wartremover" ||
          m.name == "ammonite" ||
          m.name == "kind-projector" ||
          m.name == "better-monadic-for" ||
          m.name == "mdoc"

    final val CrossScalaVersions =
      (Scala3Versions ++ Scala2Versions).distinct

    final val IncludeTest = "compile->compile;test->test"

    final val HedgehogVersion = "0.8.0"

    final val EffectieVersion = "2.0.0-beta3"

    final val CatsVersion = "2.6.1"

    final val LoggerF1Version = "1.20.0"

    final val ExtrasVersion = "0.4.0"

    final val Slf4JVersion   = "2.0.6"
    final val LogbackVersion = "1.4.5"

    final val Log4sVersion = "1.10.0"

    final val Log4JVersion = "2.19.0"
  }

lazy val libs =
  new {

    lazy val hedgehogLibs: List[ModuleID] = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion % Test
    )

    lazy val slf4jApi: ModuleID       = "org.slf4j"      % "slf4j-api"       % props.Slf4JVersion
    lazy val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % props.LogbackVersion

    lazy val log4sLib: ModuleID = "org.log4s" %% "log4s" % props.Log4sVersion

    lazy val log4jApi  = "org.apache.logging.log4j" % "log4j-api"  % props.Log4JVersion
    lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % props.Log4JVersion

    lazy val sbtLoggingLib = "org.scala-sbt" %% "util-logging"

    lazy val cats = "org.typelevel" %% "cats-core" % props.CatsVersion

    lazy val effectieCore: ModuleID        = "io.kevinlee" %% "effectie-core"         % props.EffectieVersion
    lazy val effectieSyntax: ModuleID      = "io.kevinlee" %% "effectie-syntax"       % props.EffectieVersion
    lazy val effectieCatsEffect: ModuleID  = "io.kevinlee" %% "effectie-cats-effect2" % props.EffectieVersion
    lazy val effectieCatsEffect3: ModuleID = "io.kevinlee" %% "effectie-cats-effect3" % props.EffectieVersion

    lazy val effectieMonix: ModuleID = "io.kevinlee" %% "effectie-monix3" % props.EffectieVersion

    lazy val extrasCats = "io.kevinlee" %% "extras-cats" % props.ExtrasVersion

    lazy val extrasConcurrent        = "io.kevinlee" %% "extras-concurrent"         % props.ExtrasVersion % Test
    lazy val extrasConcurrentTesting = "io.kevinlee" %% "extras-concurrent-testing" % props.ExtrasVersion % Test

    lazy val extrasHedgehogCatsEffect3 = "io.kevinlee" %% "extras-hedgehog-cats-effect3" % props.ExtrasVersion % Test
  }

// scalafmt: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def libraryDependenciesRemoveScala3Incompatible(
  scalaVersion: String,
  libraries: Seq[ModuleID]
): Seq[ModuleID] =
  (
    if (scalaVersion.startsWith("3."))
      libraries
        .filterNot(props.removeDottyIncompatible)
    else
      libraries
  )

lazy val mavenCentralPublishSettings: SettingsDefinition = List(
  /* Publish to Maven Central { */
  sonatypeCredentialHost := props.SonatypeCredentialHost,
  sonatypeRepository     := props.SonatypeRepository,
  /* } Publish to Maven Central */
)

def module(projectName: ProjectName, crossProject: CrossProject.Builder): CrossProject = {
  val prefixedName = prefixedProjectName(projectName.projectName)
  projectCommonSettings(prefixedName, crossProject)
}

def testProject(projectName: ProjectName, crossProject: CrossProject.Builder): CrossProject = {
  val prefixedName = s"test-${prefixedProjectName(projectName.projectName)}"
  projectCommonSettings(prefixedName, crossProject)
}

def projectCommonSettings(projectName: String, crossProject: CrossProject.Builder): CrossProject =
  crossProject
    .in(file(s"modules/$projectName"))
    .settings(
      name                                    := projectName,
      licenses                                := props.licenses,
      /* WartRemover and scalacOptions { */
      //      , Compile / compile / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value)
      //      , Test / compile / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value)
      wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value),
      Compile / console / wartremoverErrors   := List.empty,
      Compile / console / wartremoverWarnings := List.empty,
      Compile / console / scalacOptions       :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / wartremoverErrors      := List.empty,
      Test / console / wartremoverWarnings    := List.empty,
      Test / console / scalacOptions          :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      /* } WartRemover and scalacOptions */
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
      /* Coveralls { */
      coverageHighlighting                    := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) | Some((2, 11)) =>
          false
        case _ =>
          true
      })
      /* } Coveralls */
    )
    .settings(
      mavenCentralPublishSettings
    )
