package chromaprint

import cats.effect.{Clock, Console, IO}

object Main extends cli.App()(
  console = Console.io,
  clock = Clock.create[IO],
  fingerprinter = quick.Fingerprinter,
  httpClient = quick.httpClient
)
