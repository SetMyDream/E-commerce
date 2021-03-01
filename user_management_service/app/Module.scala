import com.google.inject.AbstractModule
import storage.UserRepository
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import storage.db.UsersTableRepository


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
