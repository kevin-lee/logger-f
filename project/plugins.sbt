logLevel := sbt.Level.Warn

addDependencyTreePlugin

addSbtPlugin("com.github.sbt"  % "sbt-ci-release"  % "1.11.1")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.3.5")
addSbtPlugin("ch.epfl.scala"   % "sbt-scalafix"    % "0.14.3")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"    % "2.5.5")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "2.3.1")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.3.2")
addSbtPlugin("org.scalameta"   % "sbt-mdoc"        % "2.3.2")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.17.0")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.16.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")

val sbtDevOopsVersion = "3.2.1"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter"   % sbtDevOopsVersion)
