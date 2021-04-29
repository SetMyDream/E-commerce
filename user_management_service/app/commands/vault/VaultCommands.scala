package commands.vault

import commands.vault.model.AppRoleCredentials
import exceptions.VaultException._
import exceptions.VaultException.TransactionalVaultException._

import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{JsArray, Json}
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
    cache
      .get[String](VaultConnection.TOKEN_CACHE_KEY)
      .map {
        case Some(token) => new VaultClient(this, token)
        case None => throw new IllegalStateException("Token wasn't found in cache")
      }
      .transform(identity, e => UnknownVaultException(e))

  def keyName(keyPostfix: String) = TOTP_KEY_PREFIX + keyPostfix

  protected[vault] def generateTOTPKey(
      authToken: String
    )(keyPostfix: String,
      accountName: String
    )(implicit ec: ExecutionContext
    ): Future[WSResponse] = {
    val payload = Json.obj(
      "generate" -> true,
      "exported" -> false,
      "issuer" -> ISSUER,
      "account_name" -> accountName
    )
    authenticatedRequest(authToken)("/totp/keys/" + keyName(keyPostfix))
      .post(payload)
      .transform(identity, e => UnknownVaultException(e))
  }

  protected[vault] def validateTOTPCode(
      authToken: String
    )(keyPostfix: String,
      code: String
    )(implicit
      ec: ExecutionContext
    ): Future[Boolean] = {
    val payload = Json.obj("code" -> code)
    for {
      res <- authenticatedRequest(authToken)("/totp/code/" + keyName(keyPostfix))
        .post(payload)
      _ <- validateVaultResponse(res).recoverWith {
        case e @ VaultErrorResponseException(resp) =>
          (resp \ "errors").get match {
            case JsArray(value) if value.head.as[String].startsWith("unknown key") =>
              Future.failed(UnknownTOTPKey)
            case _ => Future.failed(e)
          }
      }
      isValid = (res.json \ "data" \ "valid").as[Boolean]
    } yield isValid
  }

  protected[vault] def generateTOTPCode(
      authToken: String
    )(keyPostfix: String
    )(implicit
      ec: ExecutionContext
    ): Future[String] =
    for {
      res <- authenticatedRequest(authToken)("/totp/code/" + keyName(keyPostfix))
        .get()
      _ <- validateVaultResponse(res)
      code = (res.json \ "data" \ "code").as[String]
    } yield code

  protected[vault] def authenticatedRequest(authToken: String)(uri: String) =
    ws.url(API_PATH + uri).withHttpHeaders(AUTH_TOKEN_HTTP_HEADER -> authToken)

  def login(
      credentials: AppRoleCredentials
    )(implicit ec: ExecutionContext
    ): Future[String] = {
    val loginPayload = Json.obj(
      "role_id" -> credentials.role_id,
      "secret_id" -> credentials.secret_id
    )
    for {
      res <- ws.url(s"$API_PATH/auth/approle/login").post(loginPayload)
      _ <- validateVaultResponse(res)
      token = res.json \ "auth" \ "client_token"
    } yield token.as[String]
  }

  private def validateVaultResponse(resp: WSResponse): Future[WSResponse] =
    if (resp.status != 200) Future.failed(VaultErrorResponseException(resp.json))
    else Future.successful(resp)

}
