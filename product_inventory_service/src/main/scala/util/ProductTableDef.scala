package main.scala.util

import com.google.inject.Inject
import main.scala.model.Product
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class ProductTableDef(tag: Tag) extends Table[Product](tag, "product") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def mobile = column[Long]("mobile")
  def email = column[String]("email")

  override def * =
    (id, firstName, lastName, mobile, email) <>(Product.tupled, Product.unapply)
}

class Products @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                      (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  // the HasDatabaseConfigProvider trait gives access to the
  // dbConfig object that we need to run the slick queries

  val products = TableQuery[ProductTableDef]

  def add(product: Product): Future[String] = {
    dbConfig.db.run(products += product).map(res => "Product successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(products.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[Product]] = {
    dbConfig.db.run(products.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[Product]] = {
    dbConfig.db.run(products.result)
  }

}
