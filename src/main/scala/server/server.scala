package server

import cats.data.Kleisli
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.circe.syntax._
import json.Practice._
import json.resources.ADT
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
    /**
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromFile version.
     */
    case request@GET -> Root / "json" / fileName =>
      StaticFile.fromFile(new File("I:/owners/" + fileName), blocker, Some(request)).getOrElseF(NotFound())

    /**
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromResource version: added after a conversation with Arturs Sengilejevs where he suggested it as an alternative to the version above.
     * It has a caching like behavior: bad for this use case. Quote from Arturs: "It is not really cache. It is magical java classloading mechanism."
     */
    case request@GET -> Root / "json_resource_version" / fileName if List(".json").exists(fileName.endsWith) =>
      static(fileName, blocker, request)

    /**
     * Create the user query.
     */
    case GET -> Root / "query" =>
      Ok(query, `Content-Type`(MediaType.text.html))

    /**
     * Get and show the returned results to reader.
     *
     * NOTE: This is a 'proof of concept' version. At this point in time I don't need it for anything.
     */
    case GET -> Root / "results" :? dt =>
      val compareAgainst: Int = dt.get("compareAgainst").flatMap(x => x.headOption.flatMap(y => y.toIntOption)).getOrElse(0)
      val comparison: String = dt.get("comparison").flatMap(x => x.headOption).getOrElse("equal")
      val queryField: String = dt.get("queryField").flatMap(x => x.headOption).getOrElse("ID")

      val loader: List[PropertyOwner] = fetchOwnerInfoDecoded().compile.toList.unsafeRunSync()
      val filter: List[PropertyOwner] = loader.filter(x => {
        val compareThis = {
          if (queryField == "ID") Some(x.owner.ID)
          else x.owner.user_meta.wp_user_level.flatMap(_.headOption.flatten)
        }

        comparison match {
          case "larger" => compareThis.forall(_ > compareAgainst)
          case "largerEqual" => compareThis.forall(_ >= compareAgainst)
          case "smaller" =>compareThis.forall(_ < compareAgainst)
          case "smallerEqual" => compareThis.forall(_ <= compareAgainst)
          case _ => compareThis.forall(_ == compareAgainst)
        }
      })

      val lines: List[List[String]] = filter.map(x => {
        val owner: ADT.UserData = x.owner
        List(owner.ID.toString, owner.user_email, owner.display_name)
      })

      /**
       * The results are saved into file for the following reasons:
       * - create a history trail of the results received.
       * - a clean way to provide data to DataTables (https://datatables.net/) script.
       *
       * Note that the filename is not 100% unique at the moment. If there is a need for such,
       * File.createTempFile(String prefix, String suffix, File directory)
       * could be used.
       */

      val json: String = ExportJson(lines).asJson.toString()
      val filterHistoryFileName: String = (System.currentTimeMillis / 1000) + ".json"
      writePhysicalFile("I:/owners/" + filterHistoryFileName, Seq(json))

      Ok(ReturnData(filterHistoryFileName), `Content-Type`(MediaType.text.html))
  }.orNotFound

  def static(file: String, blocker: Blocker, request: Request[IO]): IO[Response[IO]] = {
    StaticFile.fromResource("owners/" + file, blocker, Some(request)).getOrElseF(NotFound())
  }

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
