package controllers

import controllers.components.{BaseFinancialController, FinancialControllerComponents}
import controllers.responces.Token
import controllers.validators.TransferValidator
import exceptions.StorageException._
import exceptions.StorageException.WalletStorageException._
import exceptions.VaultException._
import exceptions.VaultException.TransactionalVaultException._
import storage.model.WalletResource

import io.swagger.annotations._
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{ResponseHeader => _, _}
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Api(value = "User management API")
class FinancialController @Inject() (
      cc: FinancialControllerComponents
    )(implicit ec: ExecutionContext)
      extends BaseFinancialController(cc) {
  val logger = Logger(classOf[FinancialController])

  @ApiOperation(
    value = "Get the user's balance",
    response = classOf[WalletResource],
    authorizations = Array(new Authorization(Token.docsKey))
  )
  @ApiResponses(
    Array(new ApiResponse(code = 401, message = "User is not authenticated"))
  )
  def balance = silhouette.SecuredAction.async { implicit request =>
    val userId = request.identity.id
    walletResourceHandler.find(userId).map {
      case Some(wallet) => Ok(Json.toJson(wallet))
      case None =>
        logger.error(
          s"""User $userId should exist (could authenticate), 
             |but their wallet doesn't.\n
             |It might be caused by database inconsistency or
             |an error in authentication mechanism""".stripMargin
        )
        ServiceUnavailable
    }
  }

  @ApiOperation(value = "Transfer funds between two accounts")
  @ApiResponses(
    Array(
      new ApiResponse(code = 401, message = "Vault TOTP code is invalid"),
      new ApiResponse(
        code = 402,
        message = "Insufficient balance on the payer account"
      ),
      new ApiResponse(
        code = 400,
        message = "One or both users don't exist. Empty response"
      ),
      new ApiResponse(
        code = 400,
        message = "Invalid payload. Response provides details"
      )
    )
  )
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Transfer payload",
        required = true,
        dataTypeClass = classOf[TransferValidator],
        paramType = "body"
      )
    )
  )
  def transfer: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[TransferValidator]
      .map { case TransferValidator(from, to, amount, totp) =>
        walletResourceHandler.transfer(totp, from, to, amount).map(_ => Ok).recover {
          case InvalidTOTP => Unauthorized
          case InsufficientBalance => PaymentRequired
          case TransactionWithNonexistentUser => BadRequest
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
