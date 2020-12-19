name := "BH-Filters"

version := "0.8"

val circeVersion      = "0.13.0"
val http4sVersion     = "0.21.14"
val slf4jVersion      = "1.7.30"
val scalajVersion     = "2.4.2"
val fs2Version        = "2.4.6"
val scalaTestVersion  = "3.2.3"
val catsEffectVersion = "2.3.1"
val catsEffectTestVer = "0.5.0"
val spoiwoVersion     = "1.8.0"

lazy val BHFilters = (project in file("."))
  .settings(
    inThisBuild(
      List(
        scalaVersion := "2.13.4",
        scalacOptions ++= Seq(
          "-deprecation",
          "-feature",
          "-Ymacro-annotations"
        )
      )
    )
  )
  .aggregate(server, filter)

lazy val server = (project in file("server"))
  .settings(name := "BH-Server")
  .settings(libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.slf4j" % "slf4j-simple" % slf4jVersion
    ))
  .dependsOn(filter)

lazy val filter = (project in file("filter"))
  .settings(name := "BH-Filter")
  .settings(libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "com.codecommit" %% "cats-effect-testing-scalatest" % catsEffectTestVer % Test,
    "com.codecommit" %% "cats-effect-testing-scalatest-scalacheck" % catsEffectTestVer % Test,
    "co.fs2" %% "fs2-core" % fs2Version,
    "co.fs2" %% "fs2-io" % fs2Version,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion,
    "io.circe" %% "circe-optics" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-fs2" % circeVersion,
    "org.scalaj" %% "scalaj-http" % scalajVersion,
    "com.norbitltd" %% "spoiwo" % spoiwoVersion,
    ))