package main.scala

import com.google.inject.AbstractModule
import main.scala.repository.{ProductRepository, ProductTableRepository}
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}


/**
 * Guice module that tells Guice how to bind several
 * different types. This module is created when the Play
 * application starts.
 */
class Module (environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure = {
    bind[ProductRepository].to[ProductTableRepository]
  }
}
