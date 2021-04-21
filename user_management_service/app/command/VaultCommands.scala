package command

import command.model.AppRoleCredentials
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VaultCommands @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {
  val API_PATH = config.get[String]("vault.api.path")

  def login(credentials: AppRoleCredentials): Future[String] = {
    val loginPayload = Json.obj(
      "role_id" -> credentials.roleId,
      "secret_id" -> credentials.secretId
    )
    print(s"$API_PATH/auth/approle/login")
    val resp = ws.url(s"$API_PATH/auth/approle/login").post(loginPayload)
    resp.map(_.json \ "auth" \ "client_token").map(_.as[String])
  }

}
