package repository

import exception.StorageException
import model.Product

import scala.concurrent.Future

/**
 * Provides access to the user data stored somewhere
 */
trait ProductRepository {

  /**
   * Get a UserResource on a specific user by username
   */
  def get(id: Long): Future[Option[Product]]

  /**
   * Get a UserResource on a specific user by the user's id
   */
  def get(username: String): Future[Option[Product]]

  /**
   * Create a new user if the username hasn't been taken.
   * Returns an exception or id of the created user
   */
  def create(username: String): Future[Either[StorageException, Long]]

}
