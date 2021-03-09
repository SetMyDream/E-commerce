package storage.db

import exceptions.StorageException
import exceptions.StorageException.{UnknownDatabaseError}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
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
   * Slick representation of "users" table in the database
   */
  private[db] class ProductsTable(tag: Tag) extends
                    Table[ProductResource](tag, "users") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def productname = column[String]("productname", NotNull)

    def * = (id.?, productname) <>
      ((ProductResource.apply _).tupled, ProductResource.unapply)

    def idxProductname = index("idx_username", productname, unique = true)
  }

  /**
   * The starting point for all queries on the USERS table.
   */
  val users = TableQuery[ProductsTable]

  def create(username: String): Future[Either[StorageException, Long]] =
    db.run((users returning users.map(_.id)) += ProductResource(None, username))
      .map(Right(_))
      .recoverWith {
        case e: PSQLException => Future(Left(UnknownDatabaseError(cause = Some(e))))
        case e => Future.failed(e)
      }

  def get(producttitle: String): Future[Option[ProductResource]] = db.run {
    users.filter(_.productname === producttitle).result.headOption
  }

  def get(id: Long): Future[Option[ProductResource]] = db.run {
    users.filter(_.id === id).result.headOption
  }

}
