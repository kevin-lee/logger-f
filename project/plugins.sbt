logLevel := sbt.Level.Warn

addDependencyTreePlugin

addSbtPlugin("com.github.sbt"  % "sbt-ci-release"  % "1.5.10")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.21")
//addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.0.5")
addSbtPlugin("ch.epfl.scala"   % "sbt-scalafix"    % "0.10.4")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"    % "2.4.6")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "2.1.1")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.3.2")
addSbtPlugin("org.scalameta"   % "sbt-mdoc"        % "2.3.2")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.15.0")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.9.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.11")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.2.0")

addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.2")

val sbtDevOopsVersion = "3.0.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter"   % sbtDevOopsVersion)
