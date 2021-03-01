package storage

import exceptions.StorageException
import exceptions.StorageException.IllegalFieldValuesException
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


/**
 * Controls access to the backend data, returning [[UserResource]]
 */
class UserResourceHandler @Inject()(
    userRepository: UserRepository)(
    implicit ec: ExecutionContext) {

  def find(id: Long): Future[Option[UserResource]] = {
    userRepository.get(id)
  }

  def create(_username: String): Future[Either[StorageException, Long]] = {
    val username = _username.strip
    if (username.isEmpty) returnFieldErrors("username" -> "Username can't be blank")
    else if (username.length > 20) returnFieldErrors("username" -> "Username is too long")
    else if (username.length < 4) returnFieldErrors("username" -> "Username is too short")
    else userRepository.create(username)
  }

  private def returnFieldErrors(errors: (String, JsValueWrapper)*
                               ): Future[Either[IllegalFieldValuesException, Long]] =
    Future(Left(IllegalFieldValuesException(Json.obj(errors: _*))))

}
