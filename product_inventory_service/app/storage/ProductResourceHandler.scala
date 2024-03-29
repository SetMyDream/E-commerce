package storage

import exceptions.StorageException
import exceptions.StorageException.IllegalFieldValuesException

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


/** Controls access to the backend data, returning [[ProductResource]] */
class ProductResourceHandler @Inject()(productRepository: ProductRepository)(
  implicit ec: ExecutionContext) {

  def find(id: Long): Future[Option[ProductResource]] = {
    productRepository.get(id)
  }

  /** Need implement deletion. As for now, it's supposed to be through marking in db 'deleted' field as true*/
  def delete(id: Long): Future[Option[ProductResource]] = {
    productRepository.delete(id)
  }

  def create(_username: String): Future[Either[StorageException, Long]] = {
    val username = _username.strip
    username match {
      case "" => returnFieldErrors("username" -> "Username can't be blank")
      case u if u.length > 20 => returnFieldErrors("username" -> "Username is too long")
      case u if u.length < 4 => returnFieldErrors("username" -> "Username is too short")
      case u => productRepository.create(u)
    }
  }

  private def returnFieldErrors(errors: (String, JsValueWrapper)*
                               ): Future[Either[IllegalFieldValuesException, Long]] =
    Future(Left(IllegalFieldValuesException(Json.obj(errors: _*))))

}
