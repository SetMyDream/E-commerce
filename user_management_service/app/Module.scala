import javax.inject._
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import user.{DummyUserRepository, UserRepository}


/**
 * Guice module that tells Guice how to bind several
 * different types. This module is created when the Play
 * application starts.
 */
class Module (environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure = {
    bind[UserRepository].to[DummyUserRepository]
  }
}
