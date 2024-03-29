import storage.db.ProductsTableRepository
import storage.ProductRepository
import commands.vault.vaultConnection

import play.api.{Configuration, Environment}
import net.codingwell.scalaguice.ScalaModule
import com.google.inject.AbstractModule

/** Guice module that tells Guice how to bind several
 * different types. This module is created when the Play
 * application starts. */
class Module(environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure = {
    bind[ProductRepository].to[ProductsTableRepository]
    bind[VaultConnection].asEagerSingleton()
  }
}
