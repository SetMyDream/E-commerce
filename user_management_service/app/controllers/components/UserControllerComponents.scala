package controllers.components

import storage.UserResourceHandler
import auth.{DefaultEnv, UserService}

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

/**
 * Packages up the component dependencies for the post controller.
 *
 * This is a good way to minimize the surface area exposed to the controller, so the
 * controller only has to have one thing injected.
 */
final case class UserControllerComponents @Inject() (
      userResourceHandler: UserResourceHandler,
      silhouette: Silhouette[DefaultEnv],
      credentialsProvider: CredentialsProvider,
      userService: UserService,
      actionBuilder: DefaultActionBuilder,
      parsers: PlayBodyParsers,
      fileMimeTypes: FileMimeTypes,
      langs: Langs,
      messagesApi: MessagesApi,
      executionContext: ExecutionContext)
      extends ControllerComponents

/** To make integrating some UserController dependencies easier */
class BaseUserController @Inject()(ucc: UserControllerComponents)
      extends BaseController {

  override protected def controllerComponents: ControllerComponents = ucc

  def userResourceHandler: UserResourceHandler = ucc.userResourceHandler
  def silhouette: Silhouette[DefaultEnv] = ucc.silhouette
  def credentialsProvider: CredentialsProvider = ucc.credentialsProvider
  def userService: UserService = ucc.userService
}
