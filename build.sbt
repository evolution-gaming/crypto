name := "crypto"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/crypto"))

startYear := Some(2016)

organizationName := "Evolution"

organizationHomepage := Some(url("http://evolutiongaming.com"))

publishTo := Some(Resolver.evolutionReleases)

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.14", "2.12.19", "3.4.1")

libraryDependencies ++= Seq(
  "com.typesafe"   % "config"        % "1.4.3",
  "commons-codec"  % "commons-codec" % "1.15" ,
  "org.scalatest" %% "scalatest"     % "3.2.18" % Test
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))

releaseCrossBuild := true
