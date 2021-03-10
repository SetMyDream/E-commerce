package storage.db

import exceptions.StorageException
import exceptions.StorageException.UnknownDatabaseError
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.ResultSetHoldability.Default
import slick.sql.SqlProfile.ColumnOption.NotNull
import storage.{ProductRepository, ProductResource}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ProductsTableRepository @Inject()(
                                         dbConfigProvider: DatabaseConfigProvider
                                       )(implicit ec: ExecutionContext) extends ProductRepository {

  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  /**
   * Slick representation of "products" table in the database
   */
  private[db] class ProductsTable(tag: Tag) extends
    Table[ProductResource](tag, "products") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def producttitle = column[String]("productname", NotNull)

    def productdescription = column[String]("description")

    def * = (id.?, producttitle, productdescription) <>
      ((ProductResource.apply _).tupled, ProductResource.unapply)

    def idxProductname = index("idx_username", producttitle, unique = true)

    def isDeleted = column[Boolean]("deleted", O.Default(false))
  }

  /**
   * The starting point for all queries on the PRODUCTS table.
   */
  val products = TableQuery[ProductsTable]

  def create(producttitle: String): Future[Either[StorageException, Long]] =
    db.run((products returning products.map(_.id)) += ProductResource(None, producttitle, "No description"))
      .map(Right(_))
      .recoverWith {
        case e: PSQLException => Future(Left(UnknownDatabaseError(cause = Some(e))))
        case e => Future.failed(e)
      }

  def get(producttitle: String): Future[Option[ProductResource]] = db.run {
    products.filter(_.producttitle === producttitle).filter(_.isDeleted === false).result.headOption
  }

  def get(id: Long): Future[Option[ProductResource]] = db.run {
    products.filter(_.id === id).filter(_.isDeleted === false).result.headOption
  }

  /**
   * Delete a ProductResource on a specific product by id
   */
  override def delete(id: Long): Future[Int] = {
    db.run(products.filter(_.id === id).update()
  }

/**
 * Delete a ProductResource on a specific product by title
 */
override def delete (producttitle: String): Future[Int] = db.run {
  products.filter (_.producttitle === producttitle).delete
}
