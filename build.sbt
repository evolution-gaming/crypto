import sbtversionpolicy.Compatibility.BinaryCompatible

name := "crypto"

organization := "com.evolutiongaming"

homepage := Some(uri("https://github.com/evolution-gaming/crypto"))

startYear := Some(2016)

organizationName := "Evolution"

organizationHomepage := Some(uri("https://evolution.com"))

publishTo := Some(Resolver.evolutionReleases)

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.18", "3.3.8")

versionPolicyIntention := BinaryCompatible

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.9",
  "commons-codec" % "commons-codec" % "1.22.1",
  "org.scalatest" %% "scalatest" % "3.2.20" % Test,
)

licenses := Seq(("Apache-2.0", uri("https://www.apache.org/licenses/LICENSE-2.0")))

// check is called with + from the release action
addCommandAlias("check", "all versionPolicyCheck Compile/doc scalafmtCheckRepo")
addCommandAlias("fmt", "+all scalafmtRepo")
addCommandAlias("build", "all compile testFull")
