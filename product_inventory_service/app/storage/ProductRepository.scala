package storage

import exceptions.StorageException

import scala.concurrent.Future

/** Provides access to the product data */
trait ProductRepository {

  /** Get a ProductResource on a specific product by product title */
  def get(id: Long): Future[Option[ProductResource]]

  /** Get a ProductResource on a specific product by the product's id */
  def get(producttitle: String): Future[Option[ProductResource]]

  /** Create a new product if the producttitle hasn't been taken.
   * Returns an exception or id of the created user */
  def create(username: String): Future[Either[StorageException, Long]]

  /** Delete a ProductResource on a specific product by id */
  def delete(id: Long): Future[Int]

  /** Delete a ProductResource on a specific product by title */
  def delete(producttitle: String): Future[Int]
}
