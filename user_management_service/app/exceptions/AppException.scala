package exceptions

import play.api.libs.json.{JsObject, JsValue}

sealed trait AppException extends Throwable

sealed trait StorageException extends AppException
object StorageException {
  sealed trait UserStorageException extends StorageException
  object UsersStorageException {
    final case object UsernameAlreadyTaken extends UserStorageException {
      val msg = "A user with this username already exists"
    }
  }

  sealed trait WalletStorageException extends StorageException
  object WalletStorageException {
    final case object InsufficientBalance extends WalletStorageException {
      val msg = "Insufficient balance for the requested transaction"
    }
    final case object TransactionWithNonexistentUser extends WalletStorageException {
      val msg = "Tried to execute a transfer engaging a user that doesn't exist"
    }
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
  sealed trait TransactionalVaultException extends VaultException
  object TransactionalVaultException {
    case object InvalidTOTP extends TransactionalVaultException {
      val msg = "The provided TOTP code is invalid"
    }
  }

  final case class UnknownVaultException(cause: Throwable)
        extends VaultException
          with TransactionalVaultException
  final case class VaultErrorResponseException(cause: JsValue)
        extends VaultException
          with TransactionalVaultException
}
