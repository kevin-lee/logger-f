logLevel := sbt.Level.Warn

addSbtPlugin("com.geirsson"    % "sbt-ci-release"  % "1.5.7")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.13")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "1.6.0")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.2.7")
addSbtPlugin("io.kevinlee"     % "sbt-devoops"     % "2.3.0")
addSbtPlugin("org.scalameta"   % "sbt-mdoc"        % "2.2.20")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.5.0")
