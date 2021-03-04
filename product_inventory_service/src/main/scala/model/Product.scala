package main.scala.model

import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Product(id: Long, title: String, description: String, userId: Long)
class Products() {
  var products: Seq[Product] = Seq()

  def add(product: Product): Future[String] = {
    products = products :+ product.copy(id = products.length) // manual id increment
    Future("Product successfully added")
  }

  def delete(id: Long): Future[Int] = {
    val originalSize = products.length
    products = products.filterNot(_.id == id)
    Future(originalSize - products.length) // returning the number of deleted products
  }
  def get(id: Long): Future[Option[Product]] = Future(products.find(_.id == id))
  def listAll: Future[Seq[Product]] = Future(products)
}

object Product {
  implicit val format: Format[Product] = Json.format
}
