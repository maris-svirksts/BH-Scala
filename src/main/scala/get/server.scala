package get

import cats.data.Kleisli
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import java.io.File
import java.util.concurrent._
import scala.concurrent.ExecutionContext

object server extends IOApp {
  val blockingPool: ExecutorService = Executors.newFixedThreadPool(4)
  val blocker: Blocker = Blocker.liftExecutorService(blockingPool)

  val routes: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case request @ GET -> Root / "json" / fileName =>
      StaticFile.fromFile(new File("I://Repositories/BH-Scala/src/main/scala/get/owners/" + fileName), blocker, Some(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist
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