name := "BH"

version := "0.1"

scalaVersion := "2.13.4"

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" % "circe-fs2_2.13" % circeVersion,
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)
