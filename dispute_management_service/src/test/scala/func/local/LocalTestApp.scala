package func.local

import config.HttpConfig
import services.UserService
import storage.db.repo.DisputeRepository
import routes.DisputeInfoRoutes
import routes.middleware.UserAuthMiddleware

import cats.data.Kleisli
import cats.syntax.option._
import cats.effect.IO
import org.http4s.{Request, Response}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

trait LocalTestApp extends MockitoSugar with ArgumentMatchersSugar {
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
    val service = mock[UserService[IO]]
    when(service.confirm(*)) thenReturn IO.pure(userId.some)
    service
  }

}
