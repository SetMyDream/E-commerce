package auth.models

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json.Json

case class User(
      id: Long,
      username: String)
      extends Identity

object User {
  implicit val format = Json.format[User]
}
