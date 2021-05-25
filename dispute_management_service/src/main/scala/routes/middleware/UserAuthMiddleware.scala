package routes.middleware

import config.HttpConfig
import services.UserService

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware

class UserAuthMiddleware(
      httpConfig: HttpConfig,
      userService: UserService[IO]) {
  private final val AUTH_TOKEN_HEADER = httpConfig.authTokenHeader.ci

  def apply(routes: AuthedRoutes[Long, IO]): HttpRoutes[IO] = middleware(routes)

  private val produceUserId = Kleisli[OptionT[IO, *], Request[IO], Long] { req =>
    for {
      token <- OptionT.fromOption[IO](req.headers.get(AUTH_TOKEN_HEADER))
      userId <- OptionT(userService.confirm(token.value))
    } yield userId
  }

  private val middleware = AuthMiddleware(produceUserId)

}
