package repository

import exception.StorageException
import exception.StorageException.IllegalFieldValuesException
import play.api.libs.json.Json
import play.api.libs.json.Json.JsValueWrapper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


/**
 * Controls access to the backend data, returning [[Product]]
 */
class ProductResourceHandler @Inject()(
    productRepository: ProductRepository)(
    implicit ec: ExecutionContext) {

  def find(id: Long): Future[Option[Product]] = {
    productRepository.get(id)
  }

  def create(_productTitle: String, ): Future[Either[StorageException, Long]] = {
    val producttitle = _productTitle.strip
    producttitle match {
      case p if p.length > 27 => returnFieldErrors("producttitle" -> "Title is too long")
      case p => productRepository.create(p)
    }
  }

  private def returnFieldErrors(errors: (String, JsValueWrapper)*
                               ): Future[Either[IllegalFieldValuesException, Long]] =
    Future(Left(IllegalFieldValuesException(Json.obj(errors: _*))))

}
