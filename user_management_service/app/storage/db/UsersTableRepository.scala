package storage.db

import exceptions.StorageException
import exceptions.StorageException.{UnknownDatabaseError, UsernameAlreadyTaken}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.NotNull
import storage.{UserRepository, UserResource}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UsersTableRepository @Inject()(
                        dbConfigProvider: DatabaseConfigProvider
                        )(implicit ec: ExecutionContext) extends UserRepository {

  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  /**
   * Slick representation of "users" table in the database
   */
  private[db] class UsersTable(tag: Tag) extends
                    Table[UserResource](tag, "users") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", NotNull)

    def * = (id.?, username) <>
      ((UserResource.apply _).tupled, UserResource.unapply)

    def idxUsername = index("idx_username", username, unique = true)
  }

  /**
   * The starting point for all queries on the USERS table.
   */
  val users = TableQuery[UsersTable]

  def create(username: String): Future[Either[StorageException, Long]] =
    db.run((users returning users.map(_.id)) += UserResource(None, username))
      .map(Right(_))
      .recoverWith {
        case e: PSQLException if e.getMessage.startsWith(
          "ERROR: duplicate key value violates unique constraint"
        ) => Future(Left(UsernameAlreadyTaken()))
        case e: PSQLException => Future(Left(UnknownDatabaseError(cause = Some(e))))
        case e => Future.failed(e)
      }

  def get(username: String): Future[Option[UserResource]] = db.run {
    users.filter(_.username === username).result.headOption
  }

  def get(id: Long): Future[Option[UserResource]] = db.run {
    users.filter(_.id === id).result.headOption
  }

}
