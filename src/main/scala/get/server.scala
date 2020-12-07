package get

import cats.data.Kleisli
import cats.effect.{Blocker, ExitCode, IO, IOApp}
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
  val blocker: Blocker = Blocker.liftExecutorService(blockingPool)
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
                |<th>Number of Inquiries</th>
              |</tr>
            |</thead>
            |<tfoot>
              |<tr>
                |<th>Owner ID</th>
                |<th>Owner Email</th>
                |<th>Owner Name</th>
                |<th>Number of Inquiries</th>
              |</tr>
            |</tfoot>
          |</table>
          |<script>
          |$(document).ready(function() {
          |$('#results').DataTable( {
          |"ajax": 'http://127.0.0.1:8080/json/arrays.json'
          |} );
          |} );
          |</script>
        |</body>
      |</html>""".stripMargin

  val routes: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case request @ GET -> Root / "json" / fileName =>
      StaticFile.fromFile(new File("I://Repositories/BH-Scala/src/main/scala/get/owners/" + fileName), blocker, Some(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist
    case GET -> Root / "results" =>
      Ok(data, `Content-Type`(MediaType.text.html))
  }.orNotFound

  //private[http] val httpApp = { routes }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(8080, "localhost")
      .withHttpApp(routes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}