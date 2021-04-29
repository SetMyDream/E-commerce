package storage.repos

import exceptions.StorageException._
import exceptions.StorageException.WalletStorageException._
import storage.db.WalletTableRepository
import storage.model.WalletResource

import akka.Done
import org.postgresql.util.PSQLException
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WalletRepository @Inject() (
      protected val dbConfigProvider: DatabaseConfigProvider,
      val tableRepository: WalletTableRepository
    )(implicit ec: ExecutionContext)
      extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  import tableRepository._

  def get(userId: Long): Future[Option[WalletResource]] = db.run {
    wallets.filter(_.userId === userId).result.headOption
  }

  def create(walletResource: WalletResource): Future[WalletResource] =
    db.run(wallets += walletResource).map(_ => walletResource).recoverWith {
      case e: PSQLException =>
        Future.failed(UnknownDatabaseError(cause = Some(e)))
    }

  def transfer(
      from: Long,
      to: Long,
      amount: BigDecimal
    ): Future[Done] =
    db.run(transferAction(from, to, amount)).recoverWith {
      case e: PSQLException
          if e.getMessage
            .contains("violates check constraint \"balance_nonnegative\"") =>
        Future.failed(InsufficientBalance)
      case e: PSQLException =>
        Future.failed(UnknownDatabaseError(cause = Some(e)))
    }

  private def transferAction(
      from: Long,
      to: Long,
      amount: BigDecimal
    ) = {
    val action = for {
      withdrawal <- withdrawAction(from, amount)
      _ <- withdrawal match {
        case w if w == 1 => DBIO.successful(w)
        case w if w == 0 => DBIO.failed(TransactionWithNonexistentUser)
        case w =>
          DBIO.failed(
            stateErrorOnWithdraw(
              s"in transfer from user $from withdrawal=$w, to user $to"
            )
          )
      }
      refill <- topUpAction(to, amount)
      _ <- refill match {
        case r if r == 1 => DBIO.successful(r)
        case r if r == 0 => DBIO.failed(TransactionWithNonexistentUser)
        case r =>
          DBIO.failed(
            stateErrorOnWithdraw(
              s"in transfer from user $from, to user $to refill=$r"
            )
          )
      }
    } yield Done
    action.transactionally
  }

  protected[storage] def topUpAction(
      userId: BigDecimal,
      amount: BigDecimal
    ) =
    sqlu"""UPDATE "wallets" SET "balance" = "balance" + $amount WHERE "user_id" = $userId"""

  protected[storage] def withdrawAction(
      userId: BigDecimal,
      amount: BigDecimal
    ) =
    sqlu"""UPDATE "wallets" SET "balance" = "balance" - $amount WHERE "user_id" = $userId"""

  private def stateErrorOnWithdraw(when: String): UnknownDatabaseError =
    UnknownDatabaseError(
      """Database may be in an illegal state. 
      |Check primary key constraint on "wallets" table. 
      |Got more than one row updated from a withdraw action """.stripMargin
        + when,
      None
    )

}
