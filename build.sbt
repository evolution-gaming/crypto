name := "crypto"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/crypto"))

startYear := Some(2016)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.12.3", "2.11.11")

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "commons-codec" % "commons-codec" % "1.10",
  "org.scalatest" %% "scalatest" % "3.0.0" % Test
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))

releaseCrossBuild := true
