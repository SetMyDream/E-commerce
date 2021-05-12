package storage

import commands.vault.VaultCommands
import storage.model.WalletResource
import storage.repos.WalletRepository
import exceptions.StorageException._
import exceptions.StorageException.WalletStorageException._
import exceptions.VaultException._
import exceptions.VaultException.TransactionalVaultException._

import akka.Done
import play.api.Logger
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WalletResourceHandler @Inject() (
      val walletRepository: WalletRepository,
      vaultCommands: VaultCommands) {
  def logger = Logger(classOf[WalletResourceHandler])

  def find(userId: Long) = {
    walletRepository.get(userId)
  }

  def create(
      userId: Long,
      username: String,
      initialAmount: BigDecimal = 0
    )(implicit ec: ExecutionContext
    ): Future[WalletResource] =
    for {
      walletResource <- walletRepository.create(
        WalletResource(userId, initialAmount)
      )
      vaultClient <- vaultCommands.client
      vaultResponse <- vaultClient.generateTOTPKey(userId.toString, username)
      _ = if (vaultResponse.status != 204)
        logger.error(
          s"Failed to generate a Vault TOTP key for the userId $userId " +
            "with response " + vaultResponse.json.as[String]
        )
    } yield walletResource

  def transfer(
      totpCode: String,
      from: Long,
      to: Long,
      amount: BigDecimal
    )(implicit ec: ExecutionContext
    ): Future[Done] = {
    if (amount > 0)
      for {
        vaultClient <- vaultCommands.client
        codeIsValid <- vaultClient.validateTOTPCode(from.toString, totpCode)
        _ <- if (!codeIsValid) Future.failed(InvalidTOTP)
             else Future.successful(())
        _ <- walletRepository.transfer(from, to, amount)
      } yield Done
    else
      Future.failed(
        IllegalFieldValuesException(
          Json.obj("amount" -> "amount is not positive")
        )
      )
  }.recoverWith {
    case e: VaultErrorResponseException =>
      logger.error("Vault sent an unexpected error response", e)
      Future.failed(UnknownVaultException(e))
    case UnknownTOTPKey => Future.failed(TransactionWithNonexistentUser)
  }

}
