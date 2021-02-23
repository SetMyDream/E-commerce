package user.v1

import play.api.routing.Router.Routes

import javax.inject.Inject
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import user.UserController


/**
 * Routes and URLs to the [[UserController]].
 */
class UserRouter @Inject()(controller: UserController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/$id") => controller.getUser(id)
  }
}
