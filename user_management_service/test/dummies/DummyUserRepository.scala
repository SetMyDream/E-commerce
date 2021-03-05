package dummies

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import storage.{UserRepository, UserResource}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future


/**
 * Execution context for non-blocking UserRepository implementations
 */
class RepositoryExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
 * Implementation of [[UserRepository]] providing access to dummy data
 *
 * Abstract for now cause I don't want to go back here every time i change
 * the UserRepository interface
 */
@Singleton
abstract class DummyUserRepository @Inject()()(implicit ec: RepositoryExecutionContext) extends UserRepository {
  override def get(id: Long): Future[Option[UserResource]] =
    Future(Some(UserResource(Option(1L), "User123")))

  override def get(username: String): Future[Option[UserResource]] = ???
}