package exceptions.handlers

import com.mohiva.play.silhouette.api.actions.UnsecuredErrorHandler
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

/** Works with errors thrown by silhouette.UnsecuredAction */
class CustomUnsecuredErrorHandler extends UnsecuredErrorHandler {

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   */
  override def onNotAuthorized(implicit request: RequestHeader) =
    Future.successful(Forbidden)
}
