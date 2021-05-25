import config.HttpConfig
import services.ServicesHandler.Services
import storage.db.repo.DisputeRepository
import routes.middleware.UserAuthMiddleware

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.http4s.HttpApp
import org.http4s.implicits._

package object routes {

  def initHttpApp(
      httpConfig: HttpConfig,
      transactor: Transactor[IO],
      services: Services[IO]
    ): HttpApp[IO] = {
    val authMiddleware = new UserAuthMiddleware(httpConfig, services.users)
    val disputeRepository = new DisputeRepository(transactor)

    DisputeInfoRoutes(authMiddleware, disputeRepository).orNotFound
  }

}
