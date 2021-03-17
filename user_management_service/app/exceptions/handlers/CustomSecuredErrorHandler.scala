package exceptions.handlers

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Future

/** Works with errors thrown by silhouette.SecuredAction */
class CustomSecuredErrorHandler @Inject() (val messagesApi: MessagesApi)
      extends SecuredErrorHandler
        with I18nSupport
        with RequestExtractors
        with Rendering {

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   */
  override def onNotAuthenticated(implicit request: RequestHeader) =
    produceResponse(Unauthorized, "Could not authenticate")

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   */
  override def onNotAuthorized(implicit request: RequestHeader) =
    produceResponse(Forbidden, "Access denied")

  protected def produceResponse[S <: Status](
      status: S,
      msg: String
    )(implicit request: RequestHeader
    ): Future[Result] =
    Future.successful(render { case Accepts.Json() =>
      status(toJsonError(msg))
    })

  protected def toJsonError(message: String) =
    Json.obj("message" -> message)
}
