package auth.models

import com.mohiva.play.silhouette.api.Identity

case class User(id: Long, username: String) extends Identity
