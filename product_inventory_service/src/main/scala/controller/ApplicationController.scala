package main.scala.controller

import main.scala.model.form.ProductForm
import play.api.Logging
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import javax.inject.Inject

@Singleton
class ApplicationController @Inject()
(cc: ControllerComponents, productService: ProductService)
  extends AbstractController(cc) with Logging {

  def index() = Action.async { implicit request: Request[AnyContent] =>
    productService.listAllProducts map { products =>
      Ok(views.html.index(ProductForm.form, products))
    }
  }
  def filesMatching(matcher: String => Boolean) =
    for (file <- filesHere; if matcher(file.getName)) yield file

  def addProduct() = Action.async { implicit request: Request[AnyContent] =>
    ProductForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => {
        logger.warn(s"Form submission with error: ${errorForm.errors}")
        Future.successful(Ok(views.html.index(errorForm, Seq.empty[Product])))
      },
      data => {
        val newProduct = Product(0, data.firstName, data.lastName, data.mobile, data.email)
        productService.addProduct(newProduct).map( _ => Redirect(routes.ApplicationController.index()))
      })
  }

  def deleteProduct(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    productService.deleteProduct(id) map { res =>
      Redirect(routes.ApplicationController.index())
    }
  }

}