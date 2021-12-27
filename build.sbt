import ProjectInfo.{ProjectName, _}
import just.semver.SemVer
import SemVer.{Major, Minor}
import kevinlee.sbt.SbtCommon.crossVersionProps

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
    devOopsPackagedArtifacts := List(s"*/target/scala-*/${name.value}*.jar"),
    /* } GitHub Release */
  )
  .settings(mavenCentralPublishSettings)
  .settings(noPublish)
  .aggregate(
    core,
    slf4jLogger,
    log4sLogger,
    log4jLogger,
    sbtLogging,
    catsEffect,
    catsEffect3,
    scalazEffect,
    monix,
  )

lazy val core =
  projectCommonSettings("core", ProjectName("core"), file("core"))
    .settings(
      description         := "Logger for F[_] - Core",
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      )
    )

lazy val slf4jLogger = projectCommonSettings("slf4jLogger", ProjectName("slf4j"), file("slf4j"))
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

lazy val log4sLogger =
  projectCommonSettings("log4sLogger", ProjectName("log4s"), file("log4s"))
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

lazy val log4jLogger =
  projectCommonSettings("log4jLogger", ProjectName("log4j"), file("log4j"))
    .settings(
      description         := "Logger for F[_] - Logger with Log4j",
      Compile / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/main"
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
        val sharedSourceDir = baseDirectory.value / "src/test"
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

lazy val sbtLogging =
  projectCommonSettings("sbtLogging", ProjectName("sbt-logging"), file("sbt-logging"))
    .settings(
      description         := "Logger for F[_] - Logger with sbt logging",
      libraryDependencies ++= crossVersionProps(
        List.empty,
        SemVer.parseUnsafe(scalaVersion.value)
      ) {
        case (Major(2), Minor(11), _) =>
          List(
            libs.sbtLoggingLib % "1.2.4"
          ).map(_ % Provided)

        case (Major(2), Minor(12), _) =>
          List(
            libs.sbtLoggingLib % "1.3.3"
          ).map(_ % Provided)

        case (Major(2), Minor(13), _) | (Major(3), Minor(0), _) =>
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

lazy val catsEffect =
  projectCommonSettings("catsEffect", ProjectName("cats-effect"), file("cats-effect"))
    .settings(
      description         := "Logger for F[_] - Cats Effect",
      libraryDependencies ++= libs.hedgehogLibs,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
      libraryDependencies ++= Seq(libs.effectieCatsEffect)
    )
    .dependsOn(core % props.IncludeTest)

lazy val catsEffect3 =
  projectCommonSettings("catsEffect3", ProjectName("cats-effect3"), file("cats-effect3"))
    .settings(
      description         := "Logger for F[_] - Cats Effect 3",
      libraryDependencies ++= libs.hedgehogLibs,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
      libraryDependencies ++= Seq(libs.effectieCatsEffect3)
    )
    .dependsOn(core % props.IncludeTest)

lazy val monix =
  projectCommonSettings("monix", ProjectName("monix"), file(s"${props.RepoName}-monix"))
    .settings(
      description         := "Logger for F[_] - Monix",
      libraryDependencies :=
        crossVersionProps(
          libs.hedgehogLibs,
          SemVer.parseUnsafe(scalaVersion.value)
        ) {
          case (Major(2), Minor(10), _) =>
            libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover")
          case _                        =>
            libraryDependencies.value
        },
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
      libraryDependencies ++= Seq(libs.effectieMonix)
    )
    .dependsOn(core % props.IncludeTest)

lazy val scalazEffect =
  projectCommonSettings("scalazEffect", ProjectName("scalaz-effect"), file("scalaz-effect"))
    .settings(
      description         := "Logger for F[_] - Scalaz",
      libraryDependencies ++= libs.hedgehogLibs,
      libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
        scalaVersion.value,
        libraryDependencies.value
      ),
      libraryDependencies ++= Seq(libs.effectieScalazEffect)
    )
    .dependsOn(core % props.IncludeTest)

lazy val testCatsEffectWithSlf4jLogger =
  projectCommonSettings(
    "testCatsEffectWithSlf4jLogger",
    ProjectName("test-cats-effect-slf4j"),
    file("test-cats-effect-slf4j")
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
    .dependsOn(core, slf4jLogger, catsEffect)

lazy val testMonixWithSlf4jLogger =
  projectCommonSettings(
    "testMonixWithSlf4jLogger",
    ProjectName("test-monix-slf4j"),
    file("test-monix-slf4j")
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
    .dependsOn(core, slf4jLogger, monix)

lazy val testScalazEffectWithSlf4jLogger =
  projectCommonSettings(
    "testScalazEffectWithSlf4jLogger",
    ProjectName("test-scalaz-effect-slf4j"),
    file("test-scalaz-effect-slf4j")
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
    .dependsOn(core, slf4jLogger, scalazEffect)

lazy val testCatsEffectWithLog4sLogger =
  projectCommonSettings(
    "testCatsEffectWithLog4sLogger",
    ProjectName("test-cats-effect-log4s"),
    file("test-cats-effect-log4s")
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
    .dependsOn(core, log4sLogger, catsEffect)

lazy val testScalazEffectWithLog4sLogger =
  projectCommonSettings(
    "testScalazEffectWithLog4sLogger",
    ProjectName("test-scalaz-effect-log4s"),
    file("test-scalaz-effect-log4s")
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
    .dependsOn(core, log4sLogger, scalazEffect)

lazy val testCatsEffectWithLog4jLogger =
  projectCommonSettings(
    "testCatsEffectWithLog4jLogger",
    ProjectName("test-cats-effect-log4j"),
    file("test-cats-effect-log4j")
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
    .dependsOn(core, log4jLogger, catsEffect)

lazy val testScalazEffectWithLog4jLogger =
  projectCommonSettings(
    "testScalazEffectWithLog4jLogger",
    ProjectName("test-scalaz-effect-log4j"),
    file("test-scalaz-effect-log4j")
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
    .dependsOn(core, log4jLogger, scalazEffect)

lazy val docs = (project in file("generated-docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
    name                := prefixedProjectName("docs"),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    ),
    mdocVariables       := Map(
      "VERSION"                  -> {
        import sys.process._
        "git fetch --tags".!
        val tag = "git rev-list --tags --max-count=1".!!.trim
        s"git describe --tags $tag".!!.trim.stripPrefix("v")
      },
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
  .dependsOn(
    core,
    slf4jLogger,
    log4jLogger,
    sbtLogging,
    catsEffect,
    scalazEffect,
    monix,
  )

lazy val props =
  new {

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "logger-f"

    final val Scala3Versions = List("3.0.0")
    final val Scala2Versions = List("2.13.5", "2.12.13")

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

    final val hedgehogVersion = "0.8.0"

    final val effectieVersion = "1.16.0"

    final val slf4JVersion   = "1.7.30"
    final val logbackVersion = "1.2.9"

    final val log4sVersion = "1.10.0"

    final val log4JVersion = "2.17.0"
  }

lazy val libs =
  new {

    lazy val hedgehogLibs: List[ModuleID] = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test
    )

    lazy val slf4jApi: ModuleID       = "org.slf4j"      % "slf4j-api"       % props.slf4JVersion
    lazy val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % props.logbackVersion

    lazy val log4sLib: ModuleID = "org.log4s" %% "log4s" % props.log4sVersion

    lazy val log4jApi  = "org.apache.logging.log4j" % "log4j-api"  % props.log4JVersion
    lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % props.log4JVersion

    lazy val sbtLoggingLib = "org.scala-sbt" %% "util-logging"

    lazy val effectieCatsEffect: ModuleID  = "io.kevinlee" %% "effectie-cats-effect"  % props.effectieVersion
    lazy val effectieCatsEffect3: ModuleID = "io.kevinlee" %% "effectie-cats-effect3" % props.effectieVersion

    lazy val effectieMonix: ModuleID        = "io.kevinlee" %% "effectie-monix"         % props.effectieVersion
    lazy val effectieScalazEffect: ModuleID = "io.kevinlee" %% "effectie-scalaz-effect" % props.effectieVersion

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

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name                                    := prefixedProjectName(projectName.projectName),
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
        case _                             =>
          true
      })
      /* } Coveralls */
    )
    .settings(
      mavenCentralPublishSettings
    )
