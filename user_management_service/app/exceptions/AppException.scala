package exceptions

import play.api.libs.json.{JsObject, JsValue}

sealed trait AppException extends Throwable

sealed trait StorageException extends AppException
object StorageException {
  sealed trait UserStorageException extends StorageException
  object UsersStorageException {
    final case class UsernameAlreadyTaken(
          msg: String = "A user with this username already exists")
          extends UserStorageException
  }

  sealed trait WalletStorageException extends StorageException
  object WalletStorageException {
    final case class InsufficientBalance(
          msg: String = "Insufficient balance for the requested transaction")
          extends WalletStorageException
  }
  final case class IllegalFieldValuesException(
        errors: JsObject)
        extends StorageException
          with UserStorageException
          with WalletStorageException
  final case class UnknownDatabaseError(
        msg: String = "An unknown db error! View the cause exception",
        cause: Option[Throwable])
        extends StorageException
          with UserStorageException
          with WalletStorageException
}

sealed trait VaultException extends AppException
object VaultException {
  final case class UnknownVaultException(cause: Throwable) extends VaultException
  final case class VaultErrorResponseException(cause: JsValue) extends VaultException
}
