name := "crypto"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/crypto"))

startYear := Some(2016)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.1", "2.12.11")

libraryDependencies ++= Seq(
  "com.typesafe"   % "config"        % "1.4.1",
  "commons-codec"  % "commons-codec" % "1.14" ,
  "org.scalatest" %% "scalatest"     % "3.2.1" % Test
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))

releaseCrossBuild := true
