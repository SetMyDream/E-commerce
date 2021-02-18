import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.global

object App extends IOApp {
  val host = sys.env.getOrElse("SERVICE_HOST", "0.0.0.0")
  val port = sys.env.getOrElse("SERVICE_PORT", "8080").toInt

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "ping" =>
      Ok("PONG")
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(port, host)
      .withHttpApp(routes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}