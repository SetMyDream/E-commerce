package router

import controllers.ProductController

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject


/** Routes and URLs to the [[ProductController]]. */
class ProductRouter @Inject()(controller: ProductController) extends SimpleRouter {
  override def routes: Routes = {
    case POST(p"/") => controller.createProduct()
    case GET(p"/${long(id)}") => controller.getProduct(id)
    case GET(p"/") => controller.index
    case GET(p"/delete/${long(id)}") => controller.deleteProduct(id)
    case GET(p"/all") => controller.listAll()
//    case POST    /add                        controller.ProductController.addProduct
//    case GET     /delete/:id                 controller.ProductController.deleteProduct(id : Long)
  }

}
