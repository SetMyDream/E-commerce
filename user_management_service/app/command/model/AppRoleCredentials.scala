package command.model

import play.api.libs.json.Json

case class AppRoleCredentials(
      roleId: String,
      secretId: String,
      secretIdSuccessor: String,
      secretIdTTL: Long)

object AppRoleCredentials {
  implicit val format = Json.format[AppRoleCredentials]
}
