package chromaprint.cli

import cats.effect.{Clock, Console, IO, Resource}
import com.monovore.decline._
import chromaprint._
import org.http4s.client.Client

class App
(
  implicit
  console: Console[IO],
  clock: Clock[IO],
  fingerprinter: Fingerprinter.Impl,
  httpClient: Resource[IO, Client[IO]]
) extends
  CommandApp(
    Command.create.map(_.unsafeRunSync())
  )
