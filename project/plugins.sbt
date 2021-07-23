logLevel := sbt.Level.Warn

addSbtPlugin("com.geirsson"    % "sbt-ci-release"  % "1.5.7")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.15")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "1.6.0")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.2.7")
addSbtPlugin("org.scalameta"   % "sbt-mdoc"        % "2.2.22")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.6.0")

val sbtDevOopsVersion = "2.6.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
