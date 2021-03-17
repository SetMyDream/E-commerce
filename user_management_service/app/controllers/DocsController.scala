package controllers

import controllers.responces.Token

import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation
import io.swagger.annotations._
import play.api.mvc._

import javax.inject.Inject

@Api(value = "Swagger docs")
@SwaggerDefinition(
  securityDefinition = new SecurityDefinition(
    apiKeyAuthDefinitions = Array(
      new ApiKeyAuthDefinition(
        key = Token.docsKey,
        in = ApiKeyLocation.HEADER,
        name = Token.httpHeaderName,
        description = Token.docsDescription
      )
    )
  )
)
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
