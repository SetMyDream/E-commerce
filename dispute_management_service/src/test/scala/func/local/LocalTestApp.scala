package func.local

import config.HttpConfig
import services.UserService
import storage.db.repo.DisputeRepository
import routes.DisputeInfoRoutes
import routes.middleware.UserAuthMiddleware

import cats.data.Kleisli
import cats.syntax.option._
import cats.effect.IO
import doobie.util.transactor.Transactor
import org.http4s.client.Client
import org.http4s.{Request, Response, Uri}
import org.scalamock.scalatest.MockFactory

trait LocalTestApp extends MockFactory {
  final val httpConfig = HttpConfig("X-Auth-Token")

  def disputeInfoRoutes(
      repo: DisputeRepository[IO],
      service: UserService[IO] = defaultUserService(1)
    ): Kleisli[IO, Request[IO], Response[IO]] = {
    import org.http4s.implicits._
    val authMiddleware = new UserAuthMiddleware(httpConfig, service)
    DisputeInfoRoutes(authMiddleware, repo).orNotFound
  }

  def defaultUserService(userId: Long): UserService[IO] = {
    val service = stub[UserServiceIO]
    service.confirm _ when * returns IO.pure(userId.some)
    service
  }

  class UserServiceIO(
        client: Client[IO],
        baseTargetURI: Uri,
        httpConfig: HttpConfig)
        extends UserService[IO](client, baseTargetURI, httpConfig)

  abstract class DisputeRepositoryIO(transactor: Transactor[IO])
        extends DisputeRepository[IO](transactor)

}
