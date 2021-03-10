package storage

import play.api.libs.json._

/**
 * DTO for displaying user information.
 */
final case class ProductResource(id: Option[Long], producttitle: String, productdescription: String)
object ProductResource {
  /**
   * Mapping to read/write a ProductResource out as a JSON value.
   */
  implicit val format: Format[ProductResource] = Json.format
}
