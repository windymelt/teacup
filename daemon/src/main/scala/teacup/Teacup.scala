package teacup

import cats.effect.ExitCode
import cats.effect.IO
import fs2.io.net.unixsocket.UnixSocketAddress
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.http4s.swagger.SwaggerInit

final class Teacup(args: List[String]) {
  def runCommand(cmds: List[String]): IO[Int] = IO.delay {
    import os._

    val result = os
      .proc(cmds)
      .call(
        cwd = pwd,
        stdout = Inherit,
        stderr = Inherit,
        propagateEnv = true,
        check = false,
      )

    result.exitCode
  }

  def wrapWithStatusLogging(io: IO[Int]): IO[Int] = {
    io.flatMap { exitCode =>
      if (exitCode == 0) {
        scribe.cats.io.info("Script ran successfully") *> IO.pure(exitCode)
      } else {
        scribe.cats.io.error(s"Script failed with exit code: $exitCode") *> IO
          .pure(
            exitCode,
          )
      }
    }
  }

  def start = {
    val respawnStrategy = strategy.spawnNTimes[Int](5)
    respawnStrategy(wrapWithStatusLogging(runCommand(args)))
  }
}

object Teacup extends cats.effect.IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    Server.server.useForever.as(ExitCode.Success)
    // if (args.isEmpty) {
    //   scribe.cats.io.error("No arguments provided") *> IO(ExitCode.Error)
    // } else {
    //   scribe.cats.io.info(
    //     s"Running script with args: ${args.mkString(", ")}",
    //   ) *>
    //     Teacup(args).start *> IO(ExitCode.Success)
    // }
  }
}

object Server {
  import cats.effect._, org.http4s._, org.http4s.dsl.io._
  import cats.syntax.all._
  import com.comcast.ip4s._
  import org.http4s.ember.server._
  import org.http4s.implicits._
  import org.http4s.server.Router
  import scala.concurrent.duration._

  val impl: api.Teacup[IO] = new api.Teacup[IO] {
    def listDaemon(): IO[teacup.api.ListDaemonOutput] =
      IO.pure(api.ListDaemonOutput(api.DaemonId(42)))
    def runDaemon(commands: List[String]): IO[teacup.api.RunDaemonOutput] =
      Teacup(commands).start.background.allocated >> IO(
        api.RunDaemonOutput(success = true),
      )
  }
  val routesResource: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(impl).resource
  val openapiRoutes = smithy4s.http4s.swagger.docs[IO](api.Teacup)
  val server = routesResource.flatMap { routes =>
    EmberServerBuilder
      .default[IO]
      // .withUnixSocketConfig(
      //   fs2.io.net.unixsocket.UnixSockets[IO],
      //   UnixSocketAddress("/tmp/http4s-ember.sock"),
      // )
      .withHost(host"localhost")
      .withPort(port"8080")
      .withShutdownTimeout(1.seconds)
      .withHttpApp((routes <+> openapiRoutes).orNotFound)
      .build
  }
}
