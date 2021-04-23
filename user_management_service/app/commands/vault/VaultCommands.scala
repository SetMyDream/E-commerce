package commands.vault

import commands.vault.model.AppRoleCredentials

import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VaultCommands @Inject() (
      ws: WSClient,
      config: Configuration,
      cache: AsyncCacheApi) {
  val API_PATH = config.get[String]("vault.api.path")
  val TOTP_KEY_PREFIX = config.get[String]("vault.api.totp.keyPrefix")
  val AUTH_TOKEN_HTTP_HEADER = "X-Vault-Token"
  val ISSUER = "ECOMM-user-management"

  def client(implicit ec: ExecutionContext): Future[VaultClient] =
    cache.get[String](VaultConnection.TOKEN_CACHE_KEY).map {
      case Some(token) => new VaultClient(this, token)
      case None => throw new IllegalStateException("Token wasn't found in cache")
    }

  def generateTOTPKey(
      authToken: String
    )(keyPostfix: String,
      accountName: String
    ): Future[WSResponse] = {
    val payload = Json.obj(
      "generate" -> true,
      "exported" -> false,
      "issuer" -> ISSUER,
      "account_name" -> accountName
    )
    val keyName = TOTP_KEY_PREFIX + keyPostfix
    ws.url(s"$API_PATH/v1/totp/keys/$keyName")
      .withHttpHeaders(AUTH_TOKEN_HTTP_HEADER -> authToken)
      .post(payload)
  }

  def login(
      credentials: AppRoleCredentials
    )(implicit ec: ExecutionContext
    ): Future[String] = {
    val loginPayload = Json.obj(
      "role_id" -> credentials.role_id,
      "secret_id" -> credentials.secret_id
    )
    val resp = ws.url(s"$API_PATH/auth/approle/login").post(loginPayload)
    resp.map(_.json \ "auth" \ "client_token").map(_.as[String])
  }

}
