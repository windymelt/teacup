package teacup.strategy

import cats.implicits._
import cats.effect.IO

type Strategy[A] = IO[A] => IO[A]

def respawnForever[A]: Strategy[A] = io =>
  (io >> scribe.cats.io.warn(
    "respawnForeverStrategy: restarting process..."
  )).foreverM

def spawnNTimes[A](n: Int): Strategy[A] = io =>
  (n to 0 by -1)
    .zip(LazyList.continually(io))
    .take(n)
    .map { case (left, io) =>
      io <* scribe.cats.io.warn(
        s"spawnNTimesStrategy: ${left - 1} attempts left"
      )
    }
    .reduce(_ >> _)

  // TODO: error or not?
