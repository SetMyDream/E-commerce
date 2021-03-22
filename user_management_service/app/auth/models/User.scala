package auth.models

import com.mohiva.play.silhouette.api.Identity
import io.swagger.annotations.ApiModel
import play.api.libs.json.Json

@ApiModel(description = "User identity model generated by auth operations")
case class User(
      id: Long,
      username: String)
      extends Identity

object User {
  implicit val format = Json.format[User]
}
