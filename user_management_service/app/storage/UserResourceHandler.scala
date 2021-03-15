package storage

import exceptions.StorageException
import exceptions.StorageException.IllegalFieldValuesException

import cats.data.OptionT
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


/**
 * Controls access to the backend data, returning [[UserResource]]
 */
class UserResourceHandler @Inject()(
      val userRepository: UserRepository,
      authInfoRepository: AuthInfoRepository,
      passwordHasherRegistry: PasswordHasherRegistry
      )(implicit ec: ExecutionContext) {

  def find(id: Long): Future[Option[UserResource]] = {
    userRepository.get(id)
  }

  def find(username: String): OptionT[Future, UserResource] = {
    OptionT(userRepository.get(username))
  }

  def create(_username: String): Future[Either[StorageException, Long]] = {
    val username = _username.strip
    checkForLength(username, "username") match {
      case Some(err) => returnFieldErrors(Seq(err)).map(Left(_))
      case _ => userRepository.create(username)
    }
  }

  def register(
      _username: String,
      _password: String): Future[Either[StorageException, (LoginInfo, Long)]] = {
    val username = _username.strip
    val password = _password.strip

    Seq(
      checkForLength(username, "username"),
      checkForLength(password, "password")
    ).flatten match {
      case errors if errors.nonEmpty => returnFieldErrors(errors).map(Left(_))
      case _ =>
        userRepository.create(username).map(userOrError =>
          userOrError.map(userId => {
            val loginInfo = LoginInfo(CredentialsProvider.ID, userId.toString)
            val passInfo = passwordHasherRegistry.current.hash(password)
            authInfoRepository.add(loginInfo, passInfo)
            (loginInfo, userId)
          }))
    }
  }

  private def checkForLength(
    param: String,
    param_name: String,
    low: Int = 4,
    high: Int = 20,
  ): Option[(String, JsValueWrapper)] =
    param match {
      case "" => Some(param_name -> s"$param_name can't be empty")
      case p if p.length > high => Some(param_name -> s"$param_name is too long")
      case p if p.length < low => Some(param_name -> s"$param_name is too short")
      case _ => None
    }

  private def returnFieldErrors(
              errors: Seq[(String, JsValueWrapper)]
              ): Future[IllegalFieldValuesException] =
    Future.successful(IllegalFieldValuesException(Json.obj(errors: _*)))

}
