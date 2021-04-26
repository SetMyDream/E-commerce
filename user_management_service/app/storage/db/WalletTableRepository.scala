package storage.db

import storage.model.WalletResource

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.Inject

class WalletTableRepository @Inject() (
      dbConfigProvider: DatabaseConfigProvider,
      val usersTableRepository: UsersTableRepository) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class WalletTable(tag: Tag) extends Table[WalletResource](tag, "wallets") {
    def userId = column[Long]("user_id", O.PrimaryKey)
    def balance = column[BigDecimal]("balance")

    def * =
      (userId, balance) <> ((WalletResource.apply _).tupled, WalletResource.unapply)

    def userFk = foreignKey(
      "user_fk",
      userId,
      usersTableRepository.users
    )(_.id, onDelete = ForeignKeyAction.Cascade)
  }

}
