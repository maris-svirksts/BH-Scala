name := "BH"

version := "0.1"

scalaVersion := "2.13.4"

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

val circeVersion  = "0.13.0"
val http4sVersion = "0.21.13"
val slf4jVersion = "1.7.28"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "co.fs2" %% "fs2-core" % "2.4.6",
  "co.fs2" %% "fs2-io" % "2.4.6",
  "org.typelevel" %% "cats-effect" % "2.3.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" % "circe-fs2_2.13" % circeVersion,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
)
