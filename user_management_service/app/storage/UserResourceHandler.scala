package storage

import storage.model.UserResource
import storage.repos.UserRepository
import exceptions.StorageException._

import cats.implicits._
import cats.data.OptionT
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/** Controls access to the backend data, returning UserResource */
class UserResourceHandler @Inject() (
      val userRepository: UserRepository,
      authInfoRepository: AuthInfoRepository,
      passwordHasherRegistry: PasswordHasherRegistry,
      walletResourceHandler: WalletResourceHandler
    )(implicit ec: ExecutionContext) {

  def find(id: Long): Future[Option[UserResource]] = {
    userRepository.get(id)
  }

  def find(username: String): OptionT[Future, UserResource] = {
    OptionT(userRepository.get(username))
  }

  def register(
      _username: String,
      _password: String,
      initialAccount: BigDecimal = 10000
    ): Future[Either[UserStorageException, (LoginInfo, Long)]] = {
    val username = _username.trim
    val password = _password.trim

    Seq(
      checkForLength(username, "username"),
      checkForLength(password, "password")
    ).flatten match {
      case errors if errors.nonEmpty => returnFieldErrors(errors).map(Left(_))
      case _ =>
        for {
          userOrError <- userRepository.create(username)
          userInfoOrError = userOrError.map { userId =>
            val loginInfo = LoginInfo(CredentialsProvider.ID, userId.toString)
            val passInfo = passwordHasherRegistry.current.hash(password)
            authInfoRepository.add(loginInfo, passInfo)
            (loginInfo, userId)
          }
          _ <- userOrError.traverse { userId =>
            walletResourceHandler.create(userId, username, initialAccount)
          }
        } yield userInfoOrError
    }
  }

  private def checkForLength(
      param: String,
      param_name: String,
      low: Int = 4,
      high: Int = 20
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
