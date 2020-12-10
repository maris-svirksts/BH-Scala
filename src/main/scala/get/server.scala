package get

import cats.data.Kleisli
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import get.Practice.writeFile
import get.resources.{ExportJson, PropertyOwner}
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import java.io.File
import java.util.concurrent._
import scala.concurrent.ExecutionContext

object server extends IOApp {
  val blockingPool: ExecutorService = Executors.newFixedThreadPool(4)
  val blocker: Blocker              = Blocker.liftExecutorService(blockingPool)

  val data: String =
    """
      |<html>
        |<head>
          |<title>Results</title>
          |<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.22/css/jquery.dataTables.min.css">
          |<script type="text/javascript" language="javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
          |<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/1.10.22/js/jquery.dataTables.min.js"></script>
        |</head>
        |<body>
          |<table id="results" class="display" style="width:100%">
            |<thead>
              |<tr>
                |<th>Owner ID</th>
                |<th>Owner Email</th>
                |<th>Owner Name</th>
              |</tr>
            |</thead>
            |<tfoot>
              |<tr>
                |<th>Owner ID</th>
                |<th>Owner Email</th>
                |<th>Owner Name</th>
              |</tr>
            |</tfoot>
          |</table>
          |<script>
          |$(document).ready(function() {
          |$('#results').DataTable( {
          |"ajax": 'http://127.0.0.1:8080/json/results.json'
          |} );
          |} );
          |</script>
        |</body>
      |</html>""".stripMargin

  val routes: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    /*
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromFile version. Leaving it for consideration. TODO: remove after presentation.
     */
    case request @ GET -> Root / "json_file_version" / fileName =>
      StaticFile.fromFile(new File("I:/Repositories/BH/src/main/resources/owners/" + fileName), blocker, Some(request)).getOrElseF(NotFound())

    /*
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromResource version: added after a conversation with Arturs Sengilejevs where he suggested it as an alternative to the version above.
     */
    case request @ GET -> Root / "json" / fileName if List(".json").exists(fileName.endsWith) =>
      static(fileName, blocker, request)

    /*
     * Query and show the returned results to reader.
     */
    case GET -> Root / "results" / queryField / "comparison" / comparison / "compare_against" / compareAgainst =>
      val loader: List[PropertyOwner] = Practice.fetchOwnerInfoDecoded().compile.toList.unsafeRunSync()
      val filter: List[PropertyOwner] = loader.filter(x => { x.owner.ID.toInt > compareAgainst.toInt })

      val lines: List[List[String]] = for {
        i <- filter
      } yield List(i.owner.ID, i.owner.user_email, i.owner.display_name)

      val json: String = ExportJson(lines).asJson.toString()

      writeFile("src/main/resources/owners/results.json", Seq(json))
      Ok(data, `Content-Type`(MediaType.text.html))
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