package dummies

import storage.UserRepository
import storage.model.UserResource
import exceptions.StorageException
import exceptions.StorageException.UsernameAlreadyTaken

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/** Execution context for non-blocking UserRepository implementations */
class RepositoryExecutionContext @Inject() (actorSystem: ActorSystem)
      extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/** Implementation of [[UserRepository]] providing access to dummy data */
@Singleton
class DummyUserRepository @Inject() ()(implicit ec: RepositoryExecutionContext)
      extends UserRepository {
  val repo = scala.collection.mutable.Map(
    1L -> UserResource(Option(1L), "User1"),
    2L -> UserResource(Option(2L), "User2"),
    3L -> UserResource(Option(3L), "User3")
  )

  override def get(id: Long): Future[Option[UserResource]] = Future.successful {
    repo.get(id)
  }

  override def get(username: String): Future[Option[UserResource]] =
    Future.successful {
      repo.find(_._2.username == username).map(_._2)
    }

  override def create(username: String): Future[Either[StorageException, Long]] =
    get(username).map {
      case Some(user) => Left(UsernameAlreadyTaken())
      case None =>
        val key = repo.keys.max + 1L
        repo.update(key, UserResource(Some(key), username))
        Right(key)
    }
}
