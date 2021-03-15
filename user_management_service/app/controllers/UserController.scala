package controllers

import auth.models.User
import controllers.responces.Token
import controllers.validators.CredentialsValidator
import exceptions.StorageException._

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import play.api.libs.json._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/** Takes HTTP requests and produces response futures. */
class UserController @Inject() (
      cc: UserControllerComponents
    )(implicit ec: ExecutionContext)
      extends UserBaseController(cc) {

  def getUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userResourceHandler.find(id).collect {
      case Some(userResource) => Ok(Json.toJson(userResource))
      case None => NotFound
    }
  }

  def isAuthenticated: Action[AnyContent] = silhouette.UserAwareAction {
    implicit request =>
      request.identity match {
        case Some(user) => Ok(Json.toJson(user))
        case None => Forbidden("User is not authenticated")
      }
  }

  def register: Action[JsValue] = silhouette.UnsecuredAction.async(parse.json) {
    implicit request =>
      request.body
        .validate[CredentialsValidator]
        .map { user =>
          userResourceHandler.register(user.username, user.password) flatMap {
            case Right((loginInfo, userId)) =>
              val userIdentity = User(userId, user.username)
              respondWithAnAuthToken(
                loginInfo,
                SignUpEvent(userIdentity, request),
                LoginEvent(userIdentity, request)
              )
            case Left(e) =>
              e match {
                case e: UsernameAlreadyTaken =>
                  Future.successful(Conflict(e.msg))
                case e: IllegalFieldValuesException =>
                  Future.successful(BadRequest(e.errors))
                case e: UnknownDatabaseError =>
                  throw e.cause.get // ServiceUnavailable
              }
          }
        }
        .recoverTotal(err => Future.successful(BadRequest(JsError.toJson(err))))
  }

  def login: Action[CredentialsValidator] =
    silhouette.UnsecuredAction.async(parse.json[CredentialsValidator]) {
      implicit request =>
        userResourceHandler.find(request.body.username) semiflatMap { userResource =>
          val credentials =
            Credentials(userResource.id.get.toString, request.body.password)
          credentialsProvider
            .authenticate(credentials)
            .flatMap { loginInfo =>
              userService.retrieve(loginInfo).flatMap {
                case Some(user) =>
                  respondWithAnAuthToken(
                    loginInfo,
                    LoginEvent(user, request)
                  )
                case None =>
                  Future.failed(
                    new IdentityNotFoundException(
                      "Couldn't find user's login info. " +
                        "Database integrity might be disrupted"
                    )
                  )
              }
            }
            .recover {
              case _: InvalidPasswordException =>
                Forbidden("Invalid credentials")
              case _: IdentityNotFoundException => BadGateway
            }
        } getOrElse Forbidden("No such user exists")
    }

  protected def respondWithAnAuthToken(
      loginInfo: LoginInfo,
      events: SilhouetteEvent*
    )(implicit request: RequestHeader
    ): Future[AuthenticatorResult] =
    for {
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)
      token <- silhouette.env.authenticatorService.init(authenticator)
      result <- silhouette.env.authenticatorService.embed(
        token,
        Ok(
          Json.toJson(
            Token(token, authenticator.expirationDateTime)
          )
        )
      )
    } yield {
      events.foreach(silhouette.env.eventBus.publish(_))
      result
    }

  @Deprecated
  def createUser(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.flatMap { json =>
      (json \ "username").toOption
    } map { username =>
      userResourceHandler.create(username.toString).collect {
        case Right(id) => Ok(Json.obj("user_id" -> id))
        case Left(e) =>
          e match {
            case e: UsernameAlreadyTaken => Conflict(e.msg)
            case e: IllegalFieldValuesException => BadRequest(e.errors)
            case e: UnknownDatabaseError => throw e.cause.get
          }
      }
    } getOrElse Future.successful(BadRequest("Bad request format"))
  }

}
