package auth

import auth.models.User
import storage.UserRepository

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}


/**
 * A custom identity service for the [[User]] model.
 */
class UserService @Inject()(
    userRepository: UserRepository
  )(implicit ec: ExecutionContext) extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified login info.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    Try(loginInfo.providerKey.toLong) match {
      case Failure(_) => Future(None)
      case util.Success(userId) =>
        userRepository.get(userId).map(userOption =>
          userOption.map(user => User(userId, user.username))
        )
    }
  }
}
