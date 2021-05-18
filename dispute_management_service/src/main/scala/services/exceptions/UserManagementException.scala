package services.exceptions

sealed trait UserManagementException

sealed trait AuthException extends UserManagementException
object AuthException {
  case object Unauthorized extends AuthException
  case object Forbidden extends AuthException
}
