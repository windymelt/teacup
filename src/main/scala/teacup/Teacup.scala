package teacup

import cats.effect.IO
import cats.effect.ExitCode

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
        check = false
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
            exitCode
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
    if (args.isEmpty) {
      scribe.cats.io.error("No arguments provided") *> IO(ExitCode.Error)
    } else {
      scribe.cats.io.info(
        s"Running script with args: ${args.mkString(", ")}"
      ) *>
        Teacup(args).start *> IO(ExitCode.Success)
    }
  }
}
