package controllers

import exceptions.StorageException.{IllegalFieldValuesException, UnknownDatabaseError, UsernameAlreadyTaken}
import play.api.libs.json.Json
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

  def createUser(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.flatMap { json =>
      (json \ "username").toOption
    } map { username =>
      userResourceHandler.create(username.toString).collect {
        case Right(id) => Ok(Json.obj("user_id" -> id))
        case Left(e) => e match {
          case e: UsernameAlreadyTaken => BadRequest(e.msg)
          case e: IllegalFieldValuesException => BadRequest(e.errors)
          case e: UnknownDatabaseError => throw e.cause.get   // ServiceUnavailable
        }
      }
    } getOrElse Future(BadRequest("Bad request format"))
  }

}
