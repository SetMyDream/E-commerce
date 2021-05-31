package util

import config.{ClientConfig, HttpConfig}
import services.{ServiceClient, UserService}

import cats.effect.{ContextShift, IO, Resource}
import cats.syntax.applicativeError._
import org.http4s.{EntityDecoder, Method, Request}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import io.circe.Json
import io.circe.syntax.KeyOps
import org.http4s.Status.Conflict
import org.http4s.client.UnexpectedStatus

object TestingUserService {
  val testUserName = "TestUser"
  val testUserPassword = "password1234"

  def apply(
      clientConfig: ClientConfig,
      httpConfig: HttpConfig
    )(implicit S: ContextShift[IO]
    ): Resource[IO, UserService[IO]] =
    ServiceClient.res[IO](clientConfig).map { client =>
      new UserService(client, clientConfig.userManagementPath, httpConfig)
    }

  def acquireAuthToken(userService: UserService[IO]): IO[String] = {
    val body = Json.obj(
      "username" := testUserName,
      "password" := testUserPassword
    )
    val request = Request[IO](
      method = Method.POST,
      uri = userService.baseTargetURI / "register"
    ).withEntity(body)
    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
    userService.client.expect[TokenInfo](request).map(_.token).recoverWith {
      case UnexpectedStatus(Conflict) => loginDefault(userService, body)
    }
  }

  private def loginDefault(
      userService: UserService[IO],
      body: Json
    )(implicit ev: EntityDecoder[IO, TokenInfo]
    ): IO[String] = {
    val request = Request[IO](
      method = Method.POST,
      uri = userService.baseTargetURI / "login"
    ).withEntity(body)
    userService.client.expect[TokenInfo](request).map(_.token)
  }

  private case class TokenInfo(
        token: String,
        expiresOn: String)

}
