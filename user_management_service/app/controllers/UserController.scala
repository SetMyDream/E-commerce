package controllers

import auth.models.User
import controllers.responces.Token
import controllers.validators.CredentialsValidator
import exceptions.StorageException._
import exceptions.StorageException.UsersStorageException._
import storage.model.UserResource

import com.mohiva.play.silhouette.api.{Authorization => _, _}
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import io.swagger.annotations._
import play.api.libs.json._
import play.api.mvc.{ResponseHeader => _, _}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/** Takes HTTP requests and produces response futures. */
@Api(value = "User management API")
class UserController @Inject() (
      cc: UserControllerComponents
    )(implicit ec: ExecutionContext)
      extends UserBaseController(cc) {

  @ApiOperation(
    value = "Get a user using their ID",
    response = classOf[UserResource]
  )
  @ApiResponses(
    Array(new ApiResponse(code = 404, message = "User not found"))
  )
  def getUser(
      @ApiParam(value = "ID of the user to fetch", example = "1")
      id: Long
    ): Action[AnyContent] = Action.async { implicit request =>
    userResourceHandler.find(id).map {
      case Some(userResource) => Ok(Json.toJson(userResource))
      case None => NotFound("User not found")
    }
  }

  @ApiOperation(
    value = "Get a user by authentication token",
    response = classOf[User],
    authorizations = Array(
      new Authorization(Token.docsKey)
    )
  )
  @ApiResponses(
    Array(new ApiResponse(code = 401, message = "User is not authenticated"))
  )
  def isAuthenticated: Action[AnyContent] = silhouette.UserAwareAction {
    implicit request =>
      request.identity match {
        case Some(user) => Ok(Json.toJson(user))
        case None => Unauthorized("User is not authenticated")
      }
  }

  @ApiOperation(
    value = "Register and get authentication token",
    response = classOf[Token],
    responseHeaders = Array(
      new ResponseHeader(
        description = Token.docsDescription,
        name = Token.httpHeaderName,
        response = classOf[String]
      )
    )
  )
  @ApiResponses(
    Array(
      new ApiResponse(
        code = 409,
        message = "A user with this username already exists"
      ),
      new ApiResponse(
        code = 400,
        message = "If some of the json body is invalid " +
          "(provides json error)"
      ),
      new ApiResponse(
        code = 400,
        message = "If some of the fields are invalid " +
          "(provides explanation for which fields are invalid and how)"
      ),
      new ApiResponse(
        code = 500,
        message = "On an unknown database error"
      )
    )
  )
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Credentials",
        required = true,
        dataTypeClass = classOf[CredentialsValidator],
        paramType = "body"
      )
    )
  )
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
                  throw e.cause.get
              }
          }
        }
        .recoverTotal(err => Future.successful(BadRequest(JsError.toJson(err))))
  }

  @ApiOperation(
    value = "Get authentication token",
    response = classOf[Token],
    responseHeaders = Array(
      new ResponseHeader(
        description = Token.docsDescription,
        name = Token.httpHeaderName,
        response = classOf[String]
      )
    )
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 403, message = "Invalid credentials"),
      new ApiResponse(code = 403, message = "No such user exists")
    )
  )
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Credentials",
        required = true,
        dataType = "controllers.validators.CredentialsValidator",
        paramType = "body"
      )
    )
  )
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
              case _: IdentityNotFoundException =>
                Forbidden("No such user exists")
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

}
