import ProjectInfo.{ProjectName, _}
import kevinlee.sbt.SbtCommon.crossVersionProps
import just.semver.{Anh, Dsv, SemVer}
import SemVer.{Major, Minor, Patch}
import just.semver.AdditionalInfo.PreRelease

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / organization := "io.kevinlee"
ThisBuild / organizationName := "Kevin's Code"
ThisBuild / crossScalaVersions := props.CrossScalaVersions

val GitHubUsername = "Kevin-Lee"
val RepoName       = "logger-f"

ThisBuild / developers := List(
  Developer(GitHubUsername, "Kevin Lee", "kevin.code@kevinlee.io", url(s"https://github.com/$GitHubUsername"))
)
ThisBuild / homepage := Some(url(s"https://github.com/$GitHubUsername/$RepoName"))
ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      browseUrl = url(s"https://github.com/$GitHubUsername/$RepoName"),
      connection = s"scm:git:git@github.com:$GitHubUsername/$RepoName.git"
    )
  )
ThisBuild / licenses := props.licenses

ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

lazy val core = projectCommonSettings("core", ProjectName("core"), file("core"))
  .settings(
    description := "Logger for F[_] - Core",
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )

lazy val slf4jLogger = projectCommonSettings("slf4jLogger", ProjectName("slf4j"), file("slf4j"))
  .settings(
    description := "Logger for F[_] - Logger with Slf4j",
    libraryDependencies ++= Seq(
      libs.slf4jApi % Provided
    ),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .dependsOn(core)

lazy val log4sLogger = projectCommonSettings("log4sLogger", ProjectName("log4s"), file("log4s"))
  .settings(
    description := "Logger for F[_] - Logger with Log4s",
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    ),
    libraryDependencies ++= List(
      (scalaVersion.value match {
        case "3.0.0-RC1" =>
          libs.log4sLibForScala3 % Provided
        case "3.0.0-RC2" | "3.0.0-RC3" =>
          libs.log4sLibForScala3Latest % Provided
        case _ =>
          (libs.log4sLib % Provided).cross(CrossVersion.for3Use2_13)
      }),
    )
  )
  .dependsOn(core)

lazy val log4jLogger = projectCommonSettings("log4jLogger", ProjectName("log4j"), file("log4j"))
  .settings(
    description := "Logger for F[_] - Logger with Log4j",
    Compile / unmanagedSourceDirectories ++= {
      val sharedSourceDir = baseDirectory.value / "src/main"
      if (scalaVersion.value.startsWith("3.0"))
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
          sharedSourceDir / "scala-2.11_2.12",
        )
      else if (scalaVersion.value.startsWith("2.11"))
        Seq(sharedSourceDir / "scala-2.11_2.12")
      else
        Seq.empty
    },
    Test / unmanagedSourceDirectories ++= {
      val sharedSourceDir = baseDirectory.value / "src/test"
      if (scalaVersion.value.startsWith("3.0"))
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
          sharedSourceDir / "scala-2.11_2.12",
        )
      else if (scalaVersion.value.startsWith("2.11"))
        Seq(sharedSourceDir / "scala-2.11_2.12")
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

lazy val sbtLogging = projectCommonSettings("sbtLogging", ProjectName("sbt-logging"), file("sbt-logging"))
  .settings(
    description := "Logger for F[_] - Logger with sbt logging",
    libraryDependencies ++= crossVersionProps(
      List.empty,
      SemVer.parseUnsafe(scalaVersion.value)
    ) {
      case (Major(2), Minor(11), _)                           =>
        List(
          libs.sbtLoggingLib % "1.2.4"
        ).map(_ % Provided)
      case (Major(2), Minor(12), _)                           =>
        List(
          libs.sbtLoggingLib % "1.3.3"
        ).map(_ % Provided)
      case (Major(2), Minor(13), _) | (Major(3), Minor(0), _) =>
        List(
          libs.sbtLoggingLib % "1.5.0"
        ).map(_ % Provided).map(_.cross(CrossVersion.for3Use2_13))
    },
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .dependsOn(core)

lazy val catsEffect = projectCommonSettings("catsEffect", ProjectName("cats-effect"), file("cats-effect"))
  .settings(
    description := "Logger for F[_] - Cats Effect",
    libraryDependencies := (SemVer.parseUnsafe(scalaVersion.value) match {
      case SemVer(Major(2), Minor(11), _, _, _) =>
        libraryDependencies.value ++ Seq(libs.libCatsCore_2_0_0, libs.libCatsEffect_2_0_0) ++ libs.hedgehogLibsLatest
      case SemVer(
            Major(3),
            Minor(0),
            Patch(0),
            Some(PreRelease(List(Dsv(List(Anh.Alphabet("RC"), Anh.Num("1")))))),
            _
          ) =>
        libraryDependencies.value ++ Seq(libs.libCatsCore, libs.libCatsEffect2) ++ libs.hedgehogLibs
      case x                                    =>
        libraryDependencies.value ++ Seq(libs.libCatsCoreLatest, libs.libCatsEffect2Latest) ++ libs.hedgehogLibsLatest
    }),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    ),
    libraryDependencies ++= Seq(libs.effectieCatsEffect)
  )
  .dependsOn(core % props.IncludeTest)

lazy val monix = projectCommonSettings("monix", ProjectName("monix"), file(s"$RepoName-monix"))
  .settings(
    description := "Logger for F[_] - Monix",
    libraryDependencies :=
      crossVersionProps(
        libs.hedgehog(scalaVersion.value),
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

lazy val scalazEffect = projectCommonSettings("scalazEffect", ProjectName("scalaz-effect"), file("scalaz-effect"))
  .settings(
    description := "Logger for F[_] - Scalaz",
    libraryDependencies :=
      crossVersionProps(
        libs.hedgehog(scalaVersion.value),
        SemVer.parseUnsafe(scalaVersion.value)
      ) {
        case (Major(2), Minor(10), _) =>
          libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover") ++
            Seq(libs.libScalazCore, libs.libScalazEffect)
        case _                        =>
          libraryDependencies.value ++ List(libs.libScalazCore, libs.libScalazEffect).map(_.cross(CrossVersion.for3Use2_13))
      },
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    ),
    libraryDependencies ++= Seq(libs.effectieScalazEffect)
  )
  .dependsOn(core % props.IncludeTest)

lazy val testCatsEffectWithSlf4jLogger = projectCommonSettings(
  "testCatsEffectWithSlf4jLogger",
  ProjectName("test-cats-effect-slf4j"),
  file("test-cats-effect-slf4j")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Slf4j",
    libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .settings(noPublish)
  .dependsOn(core, slf4jLogger, catsEffect)

lazy val testMonixWithSlf4jLogger = projectCommonSettings(
  "testMonixWithSlf4jLogger",
  ProjectName("test-monix-slf4j"),
  file("test-monix-slf4j")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Slf4j",
    libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .settings(noPublish)
  .dependsOn(core, slf4jLogger, monix)

lazy val testScalazEffectWithSlf4jLogger = projectCommonSettings(
  "testScalazEffectWithSlf4jLogger",
  ProjectName("test-scalaz-effect-slf4j"),
  file("test-scalaz-effect-slf4j")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Slf4j",
    libraryDependencies ++= Seq(libs.slf4jApi, libs.logbackClassic),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .settings(noPublish)
  .dependsOn(core, slf4jLogger, scalazEffect)

lazy val testCatsEffectWithLog4sLogger = projectCommonSettings(
  "testCatsEffectWithLog4sLogger",
  ProjectName("test-cats-effect-log4s"),
  file("test-cats-effect-log4s")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Log4s",
    libraryDependencies ++= Seq(libs.log4sLib, libs.logbackClassic),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .settings(noPublish)
  .dependsOn(core, log4sLogger, catsEffect)

lazy val testScalazEffectWithLog4sLogger = projectCommonSettings(
  "testScalazEffectWithLog4sLogger",
  ProjectName("test-scalaz-effect-log4s"),
  file("test-scalaz-effect-log4s")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Log4s",
    libraryDependencies ++= Seq(libs.log4sLib, libs.logbackClassic),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .settings(noPublish)
  .dependsOn(core, log4sLogger, scalazEffect)

lazy val testCatsEffectWithLog4jLogger = projectCommonSettings(
  "testCatsEffectWithLog4jLogger",
  ProjectName("test-cats-effect-log4j"),
  file("test-cats-effect-log4j")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Log4j",
    libraryDependencies ++= Seq(libs.log4jApi, libs.log4jCore),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    )
  )
  .settings(noPublish)
  .dependsOn(core, log4jLogger, catsEffect)

lazy val testScalazEffectWithLog4jLogger = projectCommonSettings(
  "testScalazEffectWithLog4jLogger",
  ProjectName("test-scalaz-effect-log4j"),
  file("test-scalaz-effect-log4j")
)
  .settings(
    description := "Test Logger for F[_] - Logger with Log4j",
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
    name := prefixedProjectName("docs"),
    scalacOptions := scalacOptionsPostProcess(
      SemVer.parseUnsafe(scalaVersion.value),
      scalacOptions.value
    ),
    libraryDependencies := libraryDependenciesRemoveScala3Incompatible(
      scalaVersion.value,
      libraryDependencies.value
    ),
    mdocVariables := Map(
      "VERSION" -> {
        import sys.process._
        "git fetch --tags".!
        val tag = "git rev-list --tags --max-count=1".!!.trim
        s"git describe --tags $tag".!!.trim.stripPrefix("v")
      },
      "SUPPORTED_SCALA_VERSIONS" -> {
        val versions = props.CrossScalaVersions.map(v => s"`$v`")
        if (versions.length > 1)
          s"${versions.init.mkString(", ")} and ${versions.last}"
        else
          versions.mkString
      }
    ),
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    gitHubPagesOrgName := GitHubUsername,
    gitHubPagesRepoName := RepoName
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
    devOopsPackagedArtifacts := List(s"*/target/scala-*/${name.value}*.jar")
    /* } GitHub Release */
  )
  .settings(noPublish)
  .aggregate(
    core,
    slf4jLogger,
    log4sLogger,
    log4jLogger,
    sbtLogging,
    catsEffect,
    scalazEffect,
    monix,
  )


lazy val props = new {
  val DottyVersions = List("3.0.0-RC1", "3.0.0-RC2", "3.0.0-RC3")
  val ProjectScalaVersion = "2.13.5"

  lazy val licenses = List("MIT" -> url("http://opensource.org/licenses/MIT"))

  val removeDottyIncompatible: ModuleID => Boolean =
    m =>
      m.name == "wartremover" ||
        m.name == "ammonite" ||
        m.name == "kind-projector" ||
        m.name == "better-monadic-for" ||
        m.name == "mdoc"

  val CrossScalaVersions: Seq[String] =
    (List(
      "2.11.12",
      "2.12.13",
      ProjectScalaVersion
    ) ++ DottyVersions).distinct

  lazy val scala3cLanguageOptions =
    "-language:" + List(
      "dynamics",
      "existentials",
      "higherKinds",
      "reflectiveCalls",
      "experimental.macros",
      "implicitConversions"
    ).mkString(",")

  val IncludeTest: String = "compile->compile;test->test"

  lazy val hedgehogVersion = "0.6.6"
  lazy val hedgehogLatestVersion = "0.6.7"

  val effectieVersion: String             = "1.10.0"
}

lazy val libs = new {

  lazy val hedgehogLibs: List[ModuleID] = List(
    "qa.hedgehog" %% "hedgehog-core" % props.hedgehogVersion % Test,
    "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
    "qa.hedgehog" %% "hedgehog-sbt" % props.hedgehogVersion % Test
  )

  lazy val hedgehogLibsLatest: List[ModuleID] = List(
    "qa.hedgehog" %% "hedgehog-core" % props.hedgehogLatestVersion % Test,
    "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogLatestVersion % Test,
    "qa.hedgehog" %% "hedgehog-sbt" % props.hedgehogLatestVersion % Test
  )

  def hedgehog(scalaVersion: String): List[ModuleID] =
    if (scalaVersion == "3.0.0-RC1")
      hedgehogLibs
    else
      hedgehogLibsLatest

  lazy val libScalazCore: ModuleID = "org.scalaz" %% "scalaz-core" % "7.2.30"
  lazy val libScalazEffect: ModuleID = "org.scalaz" %% "scalaz-effect" % "7.2.30"

  lazy val libCatsCore: ModuleID = "org.typelevel" %% "cats-core" % "2.5.0"
  lazy val libCatsCoreLatest: ModuleID = "org.typelevel" %% "cats-core" % "2.6.0"

  lazy val libCatsEffect2: ModuleID = "org.typelevel" %% "cats-effect" % "2.4.1"
  lazy val libCatsEffect2Latest: ModuleID = "org.typelevel" %% "cats-effect" % "2.5.0"

  lazy val libCatsCore_2_0_0: ModuleID = "org.typelevel" %% "cats-core" % "2.0.0"
  lazy val libCatsEffect_2_0_0: ModuleID = "org.typelevel" %% "cats-effect" % "2.0.0"

  lazy val slf4jApi: ModuleID = "org.slf4j" % "slf4j-api" % "1.7.30"
  lazy val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val log4sLib: ModuleID = "org.log4s" %% "log4s" % "1.9.0"
  lazy val log4sLibForScala3: ModuleID = "org.log4s" %% "log4s" % "1.10.0-M6"
  lazy val log4sLibForScala3Latest: ModuleID = "org.log4s" %% "log4s" % "1.10.0-M7"

  lazy val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.13.1"
  lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.13.1"

  lazy val sbtLoggingLib = "org.scala-sbt" %% "util-logging"

  lazy val effectieCatsEffect: ModuleID   = "io.kevinlee" %% "effectie-cats-effect"   % props.effectieVersion
  lazy val effectieMonix: ModuleID        = "io.kevinlee" %% "effectie-monix"         % props.effectieVersion
  lazy val effectieScalazEffect: ModuleID = "io.kevinlee" %% "effectie-scalaz-effect" % props.effectieVersion

}

def prefixedProjectName(name: String) = s"$RepoName${if (name.isEmpty)
  ""
else
  s"-$name"}"

def scalacOptionsPostProcess(scalaSemVer: SemVer, options: Seq[String]): Seq[String] =
  scalaSemVer match {
    case SemVer(SemVer.Major(3), SemVer.Minor(0), _, _, _) =>
      Seq(
        "-source:3.0-migration",
        "-unchecked",
        "-Xfatal-warnings",
        props.scala3cLanguageOptions,
        "-Ykind-projector",
        "-siteroot",
        "./dotty-docs",
      )
    case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
      if (patch >= 3)
        options.filterNot(_ == "-Xlint:nullary-override")
      else
        options
    case _: SemVer                                                            =>
      options
  }

def libraryDependenciesRemoveScala3Incompatible(
  scalaVersion: String,
  libraries: Seq[ModuleID]
): Seq[ModuleID] =
  (
    if (scalaVersion.startsWith("3.0"))
      libraries
        .filterNot(props.removeDottyIncompatible)
    else
      libraries
    )

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName),
      licenses := props.licenses,
      addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.11.3" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
      scalacOptions := scalacOptionsPostProcess(
        SemVer.parseUnsafe(scalaVersion.value),
        scalacOptions.value
      ),
      Compile / doc / scalacOptions := ((Compile / doc / scalacOptions)
        .value
        .filterNot(
          if (scalaVersion.value.startsWith("3.0")) {
            Set(
              "-source:3.0-migration",
              "-scalajs",
              "-deprecation",
              "-explain-types",
              "-explain",
              "-feature",
              props.scala3cLanguageOptions,
              "-unchecked",
              "-Xfatal-warnings",
              "-Ykind-projector",
              "-from-tasty",
              "-encoding",
              "utf8",
            )
          } else {
            Set.empty[String]
          }
        )),
      scalacOptions := (SemVer.parseUnsafe(scalaVersion.value) match {
        case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
          val options = scalacOptions.value
          if (patch >= 3)
            options.filterNot(_ == "-Xlint:nullary-override")
          else
            options
        case _: SemVer                                                            =>
          scalacOptions.value
      }),
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
        case Some((2, 10)) =>
          false
        case _             =>
          true
      })
      /* } Coveralls */
    )
