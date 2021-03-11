package controllers

import controllers.responces.Token
import controllers.validators.RegisterValidator
import exceptions.StorageException._

import play.api.libs.json._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


/**
 * Takes HTTP requests and produces response futures.
 */
class UserController @Inject()(cc: UserControllerComponents)(
    implicit ec: ExecutionContext)
    extends UserBaseController(cc) {

  def getUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userResourceHandler.find(id).collect {
      case Some(userResource) => Ok(Json.toJson(userResource))
      case None => NotFound
    }
  }

  def register: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterValidator].map { user =>
      userResourceHandler.register(user.username, user.password) flatMap {
        case Right(loginInfo) =>
          for {
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(token,
              Ok(Json.toJson(
                Token(token = token,
                      expiresOn = authenticator.expirationDateTime)
                ))
            )
          } yield result
        case Left(e) => e match {
          case e: UsernameAlreadyTaken =>
            Future.successful(Conflict(e.msg))
          case e: IllegalFieldValuesException =>
            Future.successful(BadRequest(e.errors))
          case e: UnknownDatabaseError =>
            throw e.cause.get // ServiceUnavailable
        }
      }
    }.recoverTotal(err => Future.successful(BadRequest(JsError.toJson(err))))
  }

  @Deprecated
  def createUser(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.flatMap { json =>
      (json \ "username").toOption
    } map { username =>
      userResourceHandler.create(username.toString).collect {
        case Right(id) => Ok(Json.obj("user_id" -> id))
        case Left(e) => e match {
          case e: UsernameAlreadyTaken => Conflict(e.msg)
          case e: IllegalFieldValuesException => BadRequest(e.errors)
          case e: UnknownDatabaseError => throw e.cause.get   // ServiceUnavailable
        }
      }
    } getOrElse Future.successful(BadRequest("Bad request format"))
  }

}
