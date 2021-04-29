package exceptions

import play.api.libs.json.{JsObject, JsValue, Json}

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
          with WalletStorageException {
    override def getMessage =
      "Errors found in the field values:\n" + Json.prettyPrint(errors)
  }
  final case class UnknownDatabaseError(
        msg: String = "An unknown db error! View the cause exception",
        cause: Option[Throwable])
        extends StorageException
          with UserStorageException
          with WalletStorageException {
    override def getCause = cause.orNull
  }
}

sealed trait VaultException extends AppException
object VaultException {
  sealed trait TransactionalVaultException extends VaultException
  object TransactionalVaultException {
    case object InvalidTOTP extends TransactionalVaultException
  }

  final case class UnknownVaultException(
        cause: Throwable,
        msg: String = "An unknown Vault error! View the cause exception")
        extends VaultException
          with TransactionalVaultException {
    override def getCause = cause
  }
  final case class VaultErrorResponseException(cause: JsValue)
        extends VaultException
          with TransactionalVaultException {
    override def getMessage =
      "Error response from Vault:\n" + Json.prettyPrint(cause)
  }
}
