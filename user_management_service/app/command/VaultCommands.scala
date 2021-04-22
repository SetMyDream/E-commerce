package command

import command.model.AppRoleCredentials

import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VaultCommands @Inject() (
      ws: WSClient,
      config: Configuration,
      cache: AsyncCacheApi) {
  val API_PATH = config.get[String]("vault.api.path")

  def login(
      credentials: AppRoleCredentials
    )(implicit ec: ExecutionContext
    ): Future[String] = {
    val loginPayload = Json.obj(
      "role_id" -> credentials.roleId,
      "secret_id" -> credentials.secretId
    )
    val resp = ws.url(s"$API_PATH/auth/approle/login").post(loginPayload)
    resp.map(_.json \ "auth" \ "client_token").map(_.as[String])
  }

  def client(implicit ec: ExecutionContext): Future[VaultClient] =
    cache.get[String](VaultConnection.TOKEN_CACHE_KEY).map {
      case Some(token) => new VaultClient(this, token)
      case None => throw new IllegalStateException("Token wasn't found in cache")
    }

}
