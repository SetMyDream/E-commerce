package storage

import exceptions.StorageException

import scala.concurrent.Future

/**
 * Provides access to the user data stored somewhere
 */
trait ProductRepository {

  /**
   * Get a ProductResource on a specific user by username
   */
  def get(id: Long): Future[Option[ProductResource]]

  /**
   * Get a ProductResource on a specific user by the user's id
   */
  def get(producttitle: String): Future[Option[ProductResource]]

  /**
   * Create a new user if the username hasn't been taken.
   * Returns an exception or id of the created user
   */
  def create(username: String): Future[Either[StorageException, Long]]

  /**
   * Delete a ProductResource on a specific user by id
   */
  def delete(id: Long): Future[Option[ProductResource]]

  /**
   * Delete a ProductResource on a specific user by title
   */
  def delete(producttitle: String): Future[Option[ProductResource]]
}
