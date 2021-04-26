package exceptions

import play.api.libs.json.JsObject

sealed trait AppException extends Throwable

sealed trait StorageException extends AppException
object StorageException {
  final case class UsernameAlreadyTaken(
        msg: String = "A user with this username already exists")
        extends StorageException
  final case class IllegalFieldValuesException(
        errors: JsObject)
        extends StorageException
  final case class UnknownDatabaseError(
        msg: String = "An unknown db error! View the cause exception",
        cause: Option[Throwable])
        extends StorageException
}
