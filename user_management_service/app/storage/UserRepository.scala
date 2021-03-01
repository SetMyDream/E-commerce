package storage

import exceptions.StorageException

import scala.concurrent.Future


/**
 * Provides access to the user data stored somewhere
 */
trait UserRepository {

  /**
   * Get a UserResource on a specific user by username
   */
  def get(id: Long): Future[Option[UserResource]]

  /**
   * Get a UserResource on a specific user by the user's id
   */
  def get(username: String): Future[Option[UserResource]]

  /**
   * Create a new user if the username hasn't been taken.
   * Returns an exception or id of the created user
   */
  def create(username: String): Future[Either[StorageException, Long]]

}
