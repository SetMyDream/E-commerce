package auth

import auth.models.User

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator


class DefaultEnv extends Env {
  type I = User
  type A = BearerTokenAuthenticator
}
