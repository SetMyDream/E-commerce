package user

import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import play.api.mvc._


/**
 * Takes HTTP requests and produces JSON.
 */
class UserController @Inject()(cc: UserControllerComponents)(
    implicit ec: ExecutionContext)
    extends UserBaseController(cc) {

  def getUser(id: String): Action[AnyContent] = Action.async { implicit request =>
    userResourceHandler.find(id).collect {
      case Some(userResource) => Ok(Json.toJson(userResource))
      case None => NotFound
    }
  }
}
