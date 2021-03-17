package auth

import auth.models.User
import storage.PasswordInfoRepository
import exceptions.handlers.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler}
import controllers.responces.Token

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.repositories.{AuthInfoRepository, AuthenticatorRepository}
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.util.{PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.{CacheAuthenticatorRepository, DelegableAuthInfoRepository}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]

    bind[CacheLayer].to[PlayCacheLayer]
    bind[Clock].toInstance(Clock())
    bind[EventBus].toInstance(EventBus())
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasherRegistry].toInstance(
      PasswordHasherRegistry(
        current = new BCryptSha256PasswordHasher(),
        deprecated = Seq()
      )
    )

    bind[IdentityService[User]].to[UserService]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoRepository]
  }

  @Provides
  def provideEnvironment(
      userService: UserService,
      authenticatorService: AuthenticatorService[BearerTokenAuthenticator],
      eventBus: EventBus
    ): Environment[DefaultEnv] =
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )

  @Provides
  def provideAuthenticatorService(
      repository: AuthenticatorRepository[BearerTokenAuthenticator],
      idGenerator: IDGenerator,
      clock: Clock
    ): AuthenticatorService[BearerTokenAuthenticator] = {
    val settings = BearerTokenAuthenticatorSettings(
      fieldName = Token.httpHeaderName
    )
    new BearerTokenAuthenticatorService(settings, repository, idGenerator, clock)
  }

  @Provides
  def provideAuthenticatorRepository(
      cacheLayer: CacheLayer
    ): AuthenticatorRepository[BearerTokenAuthenticator] = {
    new CacheAuthenticatorRepository(cacheLayer)
  }

  @Provides
  def provideAuthInfoRepository(
      passwordInfoDao: DelegableAuthInfoDAO[PasswordInfo]
    ): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDao)
  }
}
