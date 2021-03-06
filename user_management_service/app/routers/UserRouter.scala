package routers

import controllers.UserController

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

/** Routes and URLs to the [[UserController]]. */
class UserRouter @Inject() (controller: UserController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") => controller.isAuthenticated
    case POST(p"/login") => controller.login
    case POST(p"/register") => controller.register
    case GET(p"/${long(id)}") => controller.getUser(id)
  }
}
