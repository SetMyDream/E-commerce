package commands.vault

import commands.vault.auth.CredentialsFetcher
import commands.vault.model.AppRoleCredentials

import play.api.Configuration
import play.api.cache.AsyncCacheApi

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

@Singleton
class VaultConnection @Inject() (
                                  commands: VaultCommands,
                                  config: Configuration,
                                  cache: AsyncCacheApi
                                )(implicit ec: ExecutionContext) {
  private val initialCredentialsURI = config.get[URI]("vault.auth.credentials.initFile")
  Await.result(for {
    credsJson <- CredentialsFetcher.getCredentials(initialCredentialsURI.getPath)
    credentials = credsJson.as[AppRoleCredentials]
    token <- commands.login(credentials)
    _ <- cache.set(VaultConnection.TOKEN_CACHE_KEY, token)
  } yield (), 3.seconds)
}

object VaultConnection {
  val TOKEN_CACHE_KEY = "vault.auth.token"
}
