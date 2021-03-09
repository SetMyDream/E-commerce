package controller

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import repository.ProductResourceHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext


/**
 * Packages up the component dependencies for the post controller.
 *
 * This is a good way to minimize the surface area exposed to the controller, so the
 */
case class ProductControllerComponents @Inject()(
    productResourceHandler: ProductResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    fileMimeTypes: FileMimeTypes,
    langs: Langs,
    messagesApi: MessagesApi,
    executionContext: ExecutionContext)
    extends ControllerComponents

/**
 * To make integrating some ProductController dependencies easier
 */
class ProductBaseController @Inject()(pcc: ProductControllerComponents)
    extends BaseController {

  override protected def controllerComponents: ControllerComponents = pcc

  def productResourceHandler: ProductResourceHandler = pcc.productResourceHandler
}