package commands.vault.model

import play.api.libs.json.Json
//check fields
case class AppRoleCredentials(
    role_id: String,
    secret_id: String,
    secret_id_accessor: String,
    secret_id_ttl: Long)

object AppRoleCredentials {
implicit val format = Json.format[AppRoleCredentials]
}
