package controllers

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import storage.UserResourceHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext


/**
 * Packages up the component dependencies for the post controller.
 *
 * This is a good way to minimize the surface area exposed to the controller, so the
 * controller only has to have one thing injected.
 */
case class UserControllerComponents @Inject()(
    userResourceHandler: UserResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    fileMimeTypes: FileMimeTypes,
    langs: Langs,
    messagesApi: MessagesApi,
    executionContext: ExecutionContext)
    extends ControllerComponents

/**
 * To make integrating some UserController dependencies easier
 */
class UserBaseController @Inject()(ucc: UserControllerComponents)
    extends BaseController {

  override protected def controllerComponents: ControllerComponents = ucc

  def userResourceHandler: UserResourceHandler = ucc.userResourceHandler
}
