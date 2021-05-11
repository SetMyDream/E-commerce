import cats.effect.{ExitCode, IO, IOApp}

object App extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Server.start.as(ExitCode.Success)
}
