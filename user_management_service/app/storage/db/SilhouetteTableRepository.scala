package storage.db

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}

@Singleton
class SilhouetteTableRepository @Inject() (
      dbConfigProvider: DatabaseConfigProvider,
      val usersTableRepository: UsersTableRepository) {
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  case class DBLoginInfo(
        id: Option[Long],
        providerID: String,
        providerKey: Long)
  class LoginInfoTable(tag: Tag) extends Table[DBLoginInfo](tag, "login_info") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[Long]("provider_key")

    def * = (id.?, providerId, providerKey) <>
      ((DBLoginInfo.apply _).tupled, DBLoginInfo.unapply)

    def user = foreignKey(
      "user_fk",
      providerKey,
      usersTableRepository.users
    )(_.id, onDelete = ForeignKeyAction.Cascade)
  }
  val loginInfos = TableQuery[LoginInfoTable]

  case class DBPasswordInfo(
        login_info_id: Long,
        hasher: String,
        password: String,
        salt: Option[String] = None)
  class PasswordInfoTable(tag: Tag)
        extends Table[DBPasswordInfo](tag, "password_info") {

    def loginInfoId = column[Long]("login_info_id")
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")

    def * = (loginInfoId, hasher, password, salt) <>
      ((DBPasswordInfo.apply _).tupled, DBPasswordInfo.unapply)

    def login_info = foreignKey(
      "login_info_fk",
      loginInfoId,
      loginInfos
    )(_.id, onDelete = ForeignKeyAction.Cascade)
  }
  val passwordInfoQuery = TableQuery[PasswordInfoTable]

  def loginInfoQuery(
      loginInfo: LoginInfo
    ): Query[LoginInfoTable, DBLoginInfo, Seq] = {
    val providerKey = loginInfo.providerKey.toLong
    loginInfos.filter(dbLoginInfo =>
      dbLoginInfo.providerId === loginInfo.providerID &&
        dbLoginInfo.providerKey === providerKey
    )
  }

}
