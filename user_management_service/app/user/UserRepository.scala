package user

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future


/**
 * Provides access to the user data stored somewhere
 */
trait UserRepository {
  def get(id: String): Future[Option[UserResource]]
}

/**
 * Execution context for non-blocking UserRepository implementations
 */
class RepositoryExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
 * Implementation of [[UserRepository]] providing access to dummy data
 */
@Singleton
class DummyUserRepository @Inject()()(implicit ec: RepositoryExecutionContext) extends UserRepository {
  override def get(id: String): Future[Option[UserResource]] =
    Future(Some(UserResource("1", "User1")))
}
