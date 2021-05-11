import routes.httpApp
import config._

import cats.effect._
import org.http4s.HttpApp
import org.http4s.server.{Server => BlazeServer}
import org.http4s.server.blaze._
import pureconfig.module.catseffect.loadConfigF

import scala.concurrent.ExecutionContext

class Server(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO],
      S: ContextShift[IO]) {
  def start: Resource[IO, BlazeServer[IO]] = {
    for {
      config <- config
      Config(serverConfig) = config
      server <- server(serverConfig, httpApp)
    } yield server
  }

  private[this] def server(
      config: ServerConfig,
      routes: HttpApp[IO]
    ): Resource[IO, BlazeServer[IO]] = {
    import config._
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port, host)
      .withHttpApp(routes)
      .resource
  }

  private[this] def config: Resource[IO, Config] = {
    import pureconfig.generic.auto._
    for {
      blocker <- Blocker[IO]
      config <- Resource.eval(loadConfigF[IO, Config](blocker))
    } yield config
  }

}

object Server {
  def init(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO],
      S: ContextShift[IO]
    ): Server = new Server()
}
