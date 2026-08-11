name := "crypto"

organization := "com.evolutiongaming"

homepage := Some(url("https://github.com/evolution-gaming/crypto"))

startYear := Some(2016)

organizationName := "Evolution"

organizationHomepage := Some(url("https://evolution.com"))

publishTo := Some(Resolver.evolutionReleases)

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.18", "3.3.8")

libraryDependencies ++= Seq(
  "com.typesafe"   % "config"        % "1.4.9",
  "commons-codec"  % "commons-codec" % "1.15" ,
  "org.scalatest" %% "scalatest"     % "3.2.20" % Test
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))

releaseCrossBuild := true

//addCommandAlias("check", "all versionPolicyCheck Compile/doc")
addCommandAlias("check", "show version")
addCommandAlias("build", "+all compile test")
