package chromaprint.cli

import scala.concurrent.duration.MILLISECONDS
import cats.effect.{Clock, Console, IO, Resource}
import cats.Foldable
import cats.implicits._
import chromaprint.{AudioSource, Fingerprint, Fingerprinter}
import chromaprint.acoustid.{Client => AcoustIDClient, Config => AcoustIDConfig, Response => AcoustIDResponse}
import org.http4s.client.Client

object Handler {

  def handleArgs(args: Args)
                (implicit
                 console: Console[IO],
                 clock: Clock[IO],
                 fingerprinter: Fingerprinter.Impl,
                 httpClient: Resource[IO, Client[IO]]
                ): IO[Unit] =
    Foldable[List].traverse_(args.sources)(handleSource(args.params, _))

  def handleSource(params: Args.Params, source: AudioSource)
                  (implicit
                   console: Console[IO],
                   clock: Clock[IO],
                   fingerprinter: Fingerprinter.Impl,
                   httpClient: Resource[IO, Client[IO]]
                  ): IO[Unit] =
    for {
      _ <- console.putStrLn(s"Fingerprinting ${source.name}")
      _ <- Foldable[List].traverse_((0 until params.repetitions).toList){ i =>
        for {
          startTime  <- clock.monotonic(MILLISECONDS)
          fp <- fingerprinter(params.config, source)
          finishTime <- clock.monotonic(MILLISECONDS)
          timeInSeconds = (finishTime - startTime).toFloat / 1000
          _ <- console.putStrLn(s"Generated fingerprint in ${timeInSeconds}s")
          _ <- if (params.showRaw) {
            console.putStrLn(s"Fingerprint (raw): ${fp.data}")
          } else {
            IO.unit
          }
          _ <- if (params.showCompressed) {
            console.putStrLn(s"Fingerprint (compressed): ${fp.compressed}")
          } else {
            IO.unit
          }
          _ <- if (params.showHash) {
            console.putStrLn(s"Hash: ${fp.hash}")
          } else {
            IO.unit
          }
          _ <- params.acoustId.filter(_ => i == 0) match {
            case Some(acoustId) =>
              handleAcoustIDLookup(acoustId, fp)
            case _ =>
              IO.unit
          }
        } yield ()
      }
    } yield ()

  def handleAcoustIDLookup(config: AcoustIDConfig, fingerprint: Fingerprint)
                          (implicit
                           console: Console[IO],
                           httpClient: Resource[IO, Client[IO]]
                          ): IO[Unit] =
    for {
      _ <- console.putStrLn("Looking up AcoustID database matches...")
      response <- AcoustIDClient.lookup(config, fingerprint)
      _ <- response match {
        case r: AcoustIDResponse.Error =>
          console.putError(s"AcoustID lookup failed: ${r.message}")
        case r: AcoustIDResponse.OK =>
          console.putStrLn(s"AcoustID response received:")
          if (r.isEmpty) {
            console.putError("No results were found")
          } else {
            Foldable[Vector].traverse_(r.nonEmptyResults.zipWithIndex) {
              case (result, i) =>
                for {
                  _ <- console.putStrLn(s"${result.id}: Result ${i + 1} with score ${result.score}:")
                  _ <- Foldable[Vector].traverse_(result.recordings){ recording =>
                    console.putStrLn(
                      s"${recording.id}: '${recording.title}' by '${recording.artists.map(_.name).mkString("; ")}'"
                    )
                  }
                } yield ()
            }
          }
      }
    } yield ()

}
