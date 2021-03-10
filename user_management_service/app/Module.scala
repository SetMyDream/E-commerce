import storage.UserRepository
import storage.db.UsersTableRepository

import play.api.{Configuration, Environment}
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule


/**
 * Guice module that tells Guice how to bind several
 * different types. This module is created when the Play
 * application starts.
 */
class Module (environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure = {
    bind[UserRepository].to[UsersTableRepository]
  }
}
