package teacup

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import smithy4s.http4s.SimpleRestJsonBuilder
import org.http4s.ember.client.EmberClientBuilder

object Main extends epollcat.EpollApp {
  import org.http4s.syntax.literals.uri

  val client = for {
    cr <- EmberClientBuilder.default[IO].build
    c <- SimpleRestJsonBuilder(api.Teacup)
      .client(cr)
      .uri(uri"http://localhost:8080")
      .resource
  } yield c
  override def run(args: List[String]): IO[ExitCode] = client.use { c =>
    for {
      ds <- c.listDaemon()
      _ <- IO.println(ds)
    } yield ExitCode(0)
  }
}
