import config._
import routes.initHttpApp
import storage.db.{transactorRes, Migrations}

import cats.effect._
import org.http4s.HttpApp
import org.http4s.server.{Server => BlazeServer}
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext

class Server(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO],
      S: ContextShift[IO]) {

  def start: Resource[IO, BlazeServer[IO]] = {
    for {
      Config(serverConfig, dbConfig) <- configRes[IO]
      transactor <- transactorRes[IO](dbConfig)
      _ <- Resource.eval(Migrations.applyMigrations(transactor))
      httpApp = initHttpApp(transactor)
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

}

object Server {
  def init(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO],
      S: ContextShift[IO]
    ): Server = new Server()
}
