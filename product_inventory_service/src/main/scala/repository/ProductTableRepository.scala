package main.scala.repository

import main.scala.exception.StorageException
import main.scala.exception.StorageException.UnknownDatabaseError
import main.scala.model.Product
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.NotNull

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductTableRepository @Inject()(
                                        dbConfigProvider: DatabaseConfigProvider
                                      )(implicit ec: ExecutionContext) extends ProductRepository {

  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  /**
   * Slick representation of "products" table in the database
   */
  private[db] class ProductsTable(tag: Tag) extends
    Table[Product](tag, "products") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def producttitle = column[String]("producttitle", NotNull)

    def * = (id.?, producttitle) <>
      ((ProductResource.apply _).tupled, ProductResource.unapply)

    def idxProducttitle = index("idx_producttitle", producttitle, unique = true)
  }

  val products = TableQuery[ProductsTable]

  def create(producttitle: String): Future[Either[StorageException, Long]] =
    db.run((products returning products.map(_.id)) += Product(None, producttitle, description, userId))
      .map(Right(_))
      .recoverWith {
        case e: PSQLException => Future(Left(UnknownDatabaseError(cause = Some(e))))
        case e => Future.failed(e)
      }
/**
  * Here I need to add either unique product title, or other field to identify it (DateTime, for example)
  */
  def get(producttitle: String): Future[Option[Product]] = db.run {
    products.filter(_.producttitle === producttitle).result.headOption
  }

  def get(id: Long): Future[Option[Product]] = db.run {
    products.filter(_.id === id).result.headOption
  }
}
