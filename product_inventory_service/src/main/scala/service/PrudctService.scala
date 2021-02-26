package main.scala.service

import com.google.inject.Inject
import main.scala.model.{Product, Products}

import scala.concurrent.Future

class ProductService @Inject() (products: Products) {

  def addProduct(product: Product): Future[String] = {
    products.add(product)
  }

  def deleteProduct(id: Long): Future[Int] = {
    products.delete(id)
  }

  def getProduct(id: Long): Future[Option[Product]] = {
    products.get(id)
  }

  def listAllProducts: Future[Seq[Product]] = {
    products.listAll
  }
}
