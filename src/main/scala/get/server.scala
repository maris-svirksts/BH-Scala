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

// POST query model.
object Protocol {
  final case class QueryElements(queryField: List[String], comparison: List[String], compareAgainst: List[String])
}

object server extends IOApp {
  val blockingPool: ExecutorService = Executors.newFixedThreadPool(4)
  val blocker: Blocker              = Blocker.liftExecutorService(blockingPool)

  val query: String =
    """
      |<!doctype html>
      |<html lang="en">
        |<head>
          |<meta charset="utf-8">
          |<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
          |<title>Query</title>
          |<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css" integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2" crossorigin="anonymous">
        |</head>
        |<body>
          |<div class="container">
            |<div class="row">
              |<div class="col">
                |<ul class="nav justify-content-center">
                  |<li class="nav-item">
                    |<a class="nav-link" href="http://127.0.0.1:8080/json/links.json">Master List (JSON)</a>
                  |</li>
                  |<li class="nav-item">
                    |<a class="nav-link" href="http://127.0.0.1:8080/json/106.json">Owner Example (JSON)</a>
                  |</li>
                |</ul>
              |</div>
            |</div>
            |<div class="row"><div class="col-12 p-3"><h3>Query</h3></div></div>
            |<div class="row">
              |<div class="col-md-6 col-sm-12">
                |<form action="http://127.0.0.1:8080/results" method="get">
                  |<div class="form-group">
                    |<label for="exampleFormControlInput1">Filter by Owner Data</label>
                    |<input type="text" class="form-control" id="queryField" name="queryField" placeholder="ID">
                  |</div>
                  |<div class="form-group">
                    |<label for="exampleFormControlSelect1">Comparison</label>
                    |<select class="form-control" name="comparison" id="comparison">
                      |<option value="larger">></option>
                      |<option value="largerEqual">>=</option>
                      |<option value="equal">==</option>
                      |<option value="smaller"><</option>
                      |<option value="smallerEqual"><=</option>
                    |</select>
                  |</div>
                  |<div class="form-group">
                    |<label for="compareAgainst">Compare Against</label>
                    |<input type="text" class="form-control" id="compareAgainst" name="compareAgainst" placeholder="500">
                  |</div>
                  |<div class="form-group text-right">
                    |<input class="btn btn-primary" type="submit" value="Submit">
                  |</div>
                |</form>
              |</div>
              |<div class="col-md-6 col-sm-12">
                |<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. In ut ante sit amet neque aliquam commodo. Suspendisse lacus lorem, tristique vitae arcu ac, pulvinar viverra orci. Aliquam erat volutpat. Proin vehicula a arcu a tempus. Aliquam ut elementum urna, sit amet auctor urna. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed id augue massa. Vestibulum interdum ipsum sit amet maximus eleifend. Sed ac cursus lacus. Quisque at commodo libero. Nullam feugiat ultrices accumsan. Proin nec nunc ligula. Phasellus imperdiet turpis vel nisl luctus facilisis. Nulla consectetur venenatis risus, ac pharetra ipsum sagittis eget. Sed ac quam felis.</p>
                |<p>Cras vel quam a tortor fringilla fermentum nec vitae ex. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Quisque ac risus felis. Donec dignissim consequat ante, non consectetur lacus. Duis lorem magna, dictum nec accumsan sit amet, viverra in leo. Praesent bibendum felis a elit consectetur tempor. Proin sit amet quam sem. Proin sapien tortor, rhoncus ac mauris in, convallis porta justo. Integer maximus erat nec molestie dictum. Mauris sed tortor quam. In a rutrum tortor. Vivamus rutrum libero velit, quis rutrum est pellentesque et. Donec non justo sollicitudin diam bibendum convallis nec ut nulla. Nullam at magna sed dui malesuada venenatis bibendum aliquam risus. Maecenas mollis, odio vitae pretium vestibulum, turpis mi imperdiet elit, vel mattis odio orci id sem.</p>
              |</div>
            |</div>
          |</div>
          |<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
          |<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ho+j7jyWK8fNQe+A12Hb8AhRq26LrZ/JpcUGGOn+Y7RsweNrtN/tE3MoK7ZeZDyx" crossorigin="anonymous"></script>
        |</body>
      |</html>""".stripMargin

  val data: String =
    """
      |<!doctype html>
      |<html lang="en">
        |<head>
          |<meta charset="utf-8">
          |<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
          |<title>Results</title>
          |<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.22/css/jquery.dataTables.min.css">
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
          |<script type="text/javascript" language="javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
          |<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/1.10.22/js/jquery.dataTables.min.js"></script>
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
     * StaticFile.fromFile version.
     */
    case request @ GET -> Root / "json" / fileName =>
      StaticFile.fromFile(new File("I:/Repositories/BH/src/main/resources/owners/" + fileName), blocker, Some(request)).getOrElseF(NotFound())

    /*
     * Load JSON data for reading. This part is not required if val masterSrc from Practice.scala reads it from production server.
     * StaticFile.fromResource version: added after a conversation with Arturs Sengilejevs where he suggested it as an alternative to the version above.
     * It seems to be caching files: bad for this use case.
     */
    case request @ GET -> Root / "json_resource_version" / fileName if List(".json").exists(fileName.endsWith) =>
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
      val result: Int         = dt.getOrElse("compareAgainst", Seq()).headOption.getOrElse("0").toIntOption.getOrElse(0)
      val compareAgainst: Int = if(result > 0) result else 1700

      val loader: List[PropertyOwner] = Practice.fetchOwnerInfoDecoded().compile.toList.unsafeRunSync()
      val filter: List[PropertyOwner] = loader.filter(x => { x.owner.ID.toInt > compareAgainst })

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