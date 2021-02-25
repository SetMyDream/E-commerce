package main.scala.model

case class Product(id: Long, title: String, description: String, userId: Long, emailOfSeller: String)

class Products() {

  var products: Seq[Product] = Seq()

  def add(product: Product): String = {
    products = products :+ product.copy(id = products.length) // manual id increment
    "Product successfully added"
  }

  def delete(id: Long): Option[Int] = {
    val originalSize = products.length
    products = products.filterNot(_.id == id)
    Some(originalSize - products.length) // returning the number of deleted products
  }

  def get(id: Long): Option[Product] = products.find(_.id == id)

  def listAll: Seq[Product] = products

}
