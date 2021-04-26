package storage.db

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.Inject

class WalletTableRepository @Inject() (
      dbConfigProvider: DatabaseConfigProvider,
      val usersTableRepository: UsersTableRepository) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  case class Wallet(
        userId: Long,
        balance: BigDecimal)
  class WalletTable(tag: Tag) extends Table[Wallet](tag, "wallets") {
    def userId = column[Long]("user_id", O.PrimaryKey)
    def balance = column[BigDecimal]("balance")

    def * = (userId, balance) <> ((Wallet.apply _).tupled, Wallet.unapply)

    def userFk = foreignKey(
      "user_fk",
      userId,
      usersTableRepository.users
    )(_.id, onDelete = ForeignKeyAction.Cascade)
  }

}
