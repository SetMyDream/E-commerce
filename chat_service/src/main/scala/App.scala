import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.global
import ch.qos.logback.classic.{Level,Logger}
import org.slf4j.LoggerFactory


object App extends IOApp {
  val l = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger].setLevel(Level.DEBUG)

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