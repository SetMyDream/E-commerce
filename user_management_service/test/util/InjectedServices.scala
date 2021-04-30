package util

import auth.models.User
import commands.vault.{VaultClient, VaultCommands}

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import org.scalatest.concurrent.ScalaFutures.{convertScalaFuture, PatienceConfig}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait InjectedServices { self: GuiceOneAppPerSuite =>
  implicit val ec = app.injector.instanceOf[ExecutionContext]

  def inject[A: ClassTag]: A = app.injector.instanceOf[A]

  def injectTyped[A: TypeTag]: A = inject[com.google.inject.Injector].instance[A]

  def authService: AuthenticatorService[BearerTokenAuthenticator] =
    injectTyped[AuthenticatorService[BearerTokenAuthenticator]]

  def authRepo: AuthenticatorRepository[BearerTokenAuthenticator] =
    injectTyped[AuthenticatorRepository[BearerTokenAuthenticator]]

  def identityService: IdentityService[User] =
    injectTyped[IdentityService[User]]

  def vaultClient(implicit patience: PatienceConfig): VaultClient =
    inject[VaultCommands].client.futureValue
}
