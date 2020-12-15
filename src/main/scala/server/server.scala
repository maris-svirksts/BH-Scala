package server

import cats.data.Kleisli
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.circe.syntax._
import json.Practice._
import json.resources.ADT.{ExportJson, PropertyOwner}
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import resources.HTMLPages.{ReturnData, query}
import org.http4s._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT

import java.io.File
import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContext

object server extends IOApp {
  val blockingPool: ExecutorService = Executors.newFixedThreadPool(8)
  val blocker: Blocker = Blocker.liftExecutorService(blockingPool)

  val routes: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    /*
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromFile version.
     */
    case request@GET -> Root / "json" / fileName =>
      StaticFile.fromFile(new File("I:/owners/" + fileName), blocker, Some(request)).getOrElseF(NotFound())

    /*
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromResource version: added after a conversation with Arturs Sengilejevs where he suggested it as an alternative to the version above.
     * It has a caching like behavior: bad for this use case. Quote from Arturs: "It is not really cache. It is magical java classloading mechanism."
     */
    case request@GET -> Root / "json_resource_version" / fileName if List(".json").exists(fileName.endsWith) =>
      static(fileName, blocker, request)

    /*
     * Create the user query.
     */
    case GET -> Root / "query" =>
      Ok(query, `Content-Type`(MediaType.text.html))

    /*
     * Get and show the returned results to reader.
     */
    case GET -> Root / "results" :? dt =>
      val compareAgainst: Int = dt.getOrElse("compareAgainst", Seq()).headOption.getOrElse("0").toIntOption.getOrElse(0)
      val comparison: String = dt.getOrElse("comparison", Seq()).headOption.getOrElse("equal")
      val queryField: String = dt.getOrElse("queryField", Seq()).headOption.getOrElse("ID")

      val loader: List[PropertyOwner] = fetchOwnerInfoDecoded().compile.toList.unsafeRunSync()
      val filter: List[PropertyOwner] = loader.filter(x => {
        val compareThis = if (queryField == "ID") x.owner.ID else x.owner.user_meta.wp_user_level.getOrElse(List()).headOption.get.get

        comparison match {
          case "larger" => compareThis > compareAgainst
          case "largerEqual" => compareThis >= compareAgainst
          case "smaller" => compareThis < compareAgainst
          case "smallerEqual" => compareThis <= compareAgainst
          case _ => compareThis == compareAgainst
        }
      })

      val lines: List[List[String]] = for {
        i <- filter
      } yield List(i.owner.ID.toString, i.owner.user_email, i.owner.display_name)

      /*
       * The results are saved into file for the following reasons:
       * - create a history trail of the results received.
       * - a clean way to provide data to DataTables (https://datatables.net/) script.
       */
      val json: String = ExportJson(lines).asJson.toString()
      val filterHistoryFileName: String = (System.currentTimeMillis / 1000) + ".json"
      writeFile("I:/owners/" + filterHistoryFileName, Seq(json))

      Ok(ReturnData(filterHistoryFileName), `Content-Type`(MediaType.text.html))
  }.orNotFound

  def static(file: String, blocker: Blocker, request: Request[IO]): IO[Response[IO]] = {
    StaticFile.fromResource("owners/" + file, blocker, Some(request)).getOrElseF(NotFound())
  }

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(8080, "localhost")
      .withHttpApp(routes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
