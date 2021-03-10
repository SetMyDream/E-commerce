package controllers

import exceptions.StorageException.{IllegalFieldValuesException, UnknownDatabaseError}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


/**
 * Takes HTTP requests and produces response futures.
 */
class ProductController @Inject()(cc: ProductControllerComponents)(
  implicit ec: ExecutionContext)
  extends ProductBaseController(cc) {

  def deleteProduct(id: Long): Action[AnyContent] = { implicit request =>
    productResourceHandler.delete(id).collect {
      case Some(productResource) => Ok(Json.toJson(s"${productResource.producttitle} with id ${productResource.id} successfully deleted!"))
      case None => Ok(Json.toJson(s"No product found!"))
    }
  }

  def index() = {
  }

  def getProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productResourceHandler.find(id).collect {
      case Some(productResource) => Ok(Json.toJson(productResource))
      case None => NotFound
    }
  }

  def createProduct(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.flatMap { json =>
      (json \ "productname").toOption
    } map { productname =>
      productResourceHandler.create(productname.toString).collect {
        case Right(id) => Ok(Json.obj("product_id" -> id))
        case Left(e) => e match {
          case e: IllegalFieldValuesException => BadRequest(e.errors)
          case e: UnknownDatabaseError => throw e.cause.get // ServiceUnavailable
        }
      }
    } getOrElse Future(BadRequest("Bad request format"))
  }

}
