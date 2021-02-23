package user

import javax.inject.{Inject}
import scala.concurrent.{ExecutionContext, Future}


/**
 * Controls access to the backend data, returning [[UserResource]]
 */
class UserResourceHandler @Inject()(
    userRepository: UserRepository)(
    implicit ec: ExecutionContext) {

  def find(id: String): Future[Option[UserResource]] = {
    if (id.isBlank) Future(None)
    else userRepository.get(id)
  }

}
