package services

import config.ClientConfig
import cats.effect.{ConcurrentEffect, Resource}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ServiceClient {

  def res[F[_]](
      config: ClientConfig
    )(implicit F: ConcurrentEffect[F]
    ): Resource[F, Client[F]] = {
    val clientPool = Resource.make(
      F.delay(Executors.newFixedThreadPool(config.poolSize))
    )(ex => F.delay(ex.shutdown()))
    val clientExecutor = clientPool.map(ExecutionContext.fromExecutor)
    clientExecutor.flatMap(ec =>
      BlazeClientBuilder[F](ec).withMaxWaitQueueLimit(1024).resource
    )
  }

}
