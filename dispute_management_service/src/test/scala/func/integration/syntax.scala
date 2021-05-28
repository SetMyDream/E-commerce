package func.integration

import cats.effect.IO
import org.scalactic.source
import org.scalatest.concurrent.ScalaFutures.{convertScalaFuture, PatienceConfig}
import org.scalatest.time.{Seconds, Span}

object syntax {

  implicit def ioToFutureValue[A](io: IO[A]) = new FutureValueOps(io) {
    override def futureValue(implicit pos: source.Position) = {
      val integrationPatience = PatienceConfig(Span(50, Seconds))
      io.unsafeToFuture().futureValue(integrationPatience, pos)
    }
  }

  abstract class FutureValueOps[F[_], A](private val f: F[A]) {
    def futureValue(implicit pos: source.Position): A
  }

}
