package controllers

import controllers.components.{BaseFinancialController, FinancialControllerComponents}
import controllers.validators.TransferValidator
import exceptions.StorageException._
import exceptions.StorageException.WalletStorageException._
import exceptions.VaultException._
import exceptions.VaultException.TransactionalVaultException._

import play.api.libs.json.{JsError, JsValue}
import play.api.mvc.{ResponseHeader => _, _}
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialController @Inject() (
      cc: FinancialControllerComponents
    )(implicit ec: ExecutionContext)
      extends BaseFinancialController(cc) {
  val logger = Logger(classOf[FinancialController])

  def transfer: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[TransferValidator]
      .map { case TransferValidator(from, to, amount, totp) =>
        walletResourceHandler.transfer(totp, from, to, amount).map(_ => Ok).recover {
          case InvalidTOTP => Unauthorized
          case InsufficientBalance => PaymentRequired
          case IllegalFieldValuesException(err) => BadRequest(err)
          case UnknownVaultException(cause, msg) =>
            logger.error(msg, cause)
            ServiceUnavailable
          case UnknownDatabaseError(msg, Some(cause)) =>
            logger.error(msg, cause)
            ServiceUnavailable
        }
      }
      .recoverTotal(err => Future.successful(BadRequest(JsError.toJson(err))))
  }

}
