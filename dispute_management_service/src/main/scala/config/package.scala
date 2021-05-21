import cats.effect._
import pureconfig.module.catseffect.loadConfigF

package object config {
  def configRes[F[_]: ConcurrentEffect: ContextShift]: Resource[F, Config] = {
    import pureconfig.generic.auto._
    import pureconfig.module.http4s._
    for {
      blocker <- Blocker[F]
      config <- Resource.eval(loadConfigF[F, Config](blocker))
    } yield config
  }
}
