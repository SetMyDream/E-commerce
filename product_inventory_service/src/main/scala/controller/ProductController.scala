package main.scala.controller

import main.scala.model.Product
import main.scala.model.form.ProductForm
import main.scala.service.ProductService
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}
import play.api.routing.Router.empty.routes

import javax.inject.Inject
import scala.concurrent.Future

@Singleton
class ProductController @Inject()
(cc: ControllerComponents, productService: ProductService)
  extends AbstractController(cc) with Logging {

  def index() = Action.async { implicit request: Request[AnyContent] =>
    productService.listAllProducts map { products =>
      Ok(main.scala.views.index(ProductForm.form, products))
    }
  }

  def getProduct (id: Long): Action[AnyContent] = Action.async { implicit request =>
    productResourceHandler.find(id).collect {
      case Some(userResource) => Ok(Json.toJson(userResource))
      case None => NotFound
    }
  }

  def addProduct() = Action.async { implicit request: Request[AnyContent] =>
    ProductForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => {
        logger.warn(s"Form submission with error: ${errorForm.errors}")
        Future.successful(Ok (views.html.index(errorForm, Seq.empty[Product])))
      },
      data => {
        val newProduct = Product(0, data.title, data.description, data.userId, data.emailOfSeller)
        productService.addProduct(newProduct).map( _ => Redirect(routes.ApplicationController.index()))
      })
  }

  def deleteProduct(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    productService.deleteProduct(id) map { res =>
      Redirect(routes.ApplicationController.index())
    }
  }

}