package service

import com.google.inject.Inject
import model.Products

import scala.concurrent.Future

class ProductService @Inject() (products: Products) {

  def addProduct(product: model.Product): Future[String] = {
    products.add(product)
  }

  def deleteProduct(id: Long): Future[Int] = {
    products.delete(id)
  }

  def getProduct(id: Long): Future[Option[model.Product]] = {
    products.get(id)
  }

  def listAllProducts: Future[Seq[model.Product]] = {
    products.listAll
  }
}
