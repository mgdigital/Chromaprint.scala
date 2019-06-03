package chromaprint.cli

import cats.effect.{Clock, Console, IO, Resource}
import com.monovore.decline
import chromaprint._
import org.http4s.client.Client

object Command {

  private[chromaprint] lazy val args: decline.Command[Args] =
    decline.Command(
      name = "chromaprint",
      header = "Chromaprint CLI"
    )(Parser.args)

  def create
  (
    implicit
    console: Console[IO],
    clock: Clock[IO],
    fingerprinter: Fingerprinter.Impl,
    httpClient: Resource[IO, Client[IO]]
  ): decline.Command[IO[Unit]] =
    args.map(
      Handler.handleArgs(_)(
        console = console,
        clock = clock,
        fingerprinter = fingerprinter,
        httpClient = httpClient
      )
    )
}
