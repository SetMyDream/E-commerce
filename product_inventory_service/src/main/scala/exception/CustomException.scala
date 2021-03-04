package main.scala.exception

import play.api.libs.json.JsObject

sealed trait CustomException

sealed trait StorageException extends CustomException
object StorageException {
  final case class IllegalFieldValuesException(errors: JsObject) extends StorageException
  final case class UnknownDatabaseError(msg: String = "An unknown db error! View the cause exception", cause: Option[Throwable]) extends StorageException
}
