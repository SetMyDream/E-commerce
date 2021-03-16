package controllers

import io.swagger.annotations.{Api, ApiOperation}
import play.api.mvc._

import javax.inject.Inject

@Api(value = "Swagger docs")
class DocsController @Inject() (components: ControllerComponents)
      extends AbstractController(components) {

  @ApiOperation(value = "", hidden = true)
  def redirectDocs = Action { implicit request =>
    Redirect(
      url = "/assets/lib/swagger-ui/index.html",
      queryStringParams =
        Map("url" -> Seq("http://" + request.host + "/swagger.json"))
    )
  }
}
