package storage

import exceptions.StorageException._
import exceptions.StorageException.WalletStorageException._
import storage.db.WalletTableRepository
import storage.model.WalletResource

import org.postgresql.util.PSQLException
import slick.jdbc.JdbcProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

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
    db.run(wallets += walletResource)
      .transform(
        _ => walletResource,
        {
          case e: PSQLException => UnknownDatabaseError(cause = Some(e))
          case e => e
        }
      )

  def transfer(
      from: Long,
      to: Long,
      amount: BigDecimal
    ): Future[Unit] = db.run {
    val action = for {
      _ <- withdrawAction(from, amount)
      _ <- topUpAction(to, amount)
    } yield ()
    action.transactionally
  }.transform(
    identity,
    {
      case e: PSQLException
          if e.getMessage
            .contains("violates check constraint \"balance_nonnegative\"") =>
        InsufficientBalance()
      case e: PSQLException => UnknownDatabaseError(cause = Some(e))
      case e => e
    }
  )

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

}
