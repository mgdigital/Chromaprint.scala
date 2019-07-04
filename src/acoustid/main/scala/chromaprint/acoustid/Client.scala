package chromaprint.acoustid

import cats.effect._
import org.http4s
import http4s._
import http4s.circe._
import http4s.client._

import chromaprint.Fingerprint

object Client {

  object Params {

    val defaultMeta: Set[Metadata.MetadataGroup] = Set(
      Metadata.recordings
    )

    def apply(fingerprint: Fingerprint): Params =
      Params(fingerprint, defaultMeta)
  }

  final case class Params
  (
    fingerprint: Fingerprint,
    meta: Set[Metadata.MetadataGroup]
  )

  final case class Request(config: Config, params: Params)

  def createUri(request: Request): Uri =
    Uri().resolve(request.config.baseUri).
        withOptionQueryParam("client", request.config.clientId).
        withOptionQueryParam("meta", Some(request.params.meta).filter(_.nonEmpty).map(_.mkString(" "))).
        withQueryParam("duration", request.params.fingerprint.trackDuration.toInt.toString).
        withQueryParam("fingerprint", request.params.fingerprint.compressed)

  def transformRequest(request: Request): IO[http4s.Request[IO]] =
    IO(http4s.Request[IO](
      Method.GET,
      createUri(request)
    ))

  import Decoder._

  implicit val responseDecoder: EntityDecoder[IO, Response] = jsonOf[IO, Response]

  def lookup(config: Config, fingerprint: Fingerprint)(implicit httpClient: Resource[IO, Client[IO]]): IO[Response] =
    lookup(Request(config, Params(fingerprint)))

  def lookup(request: Request)(implicit httpClient: Resource[IO, Client[IO]]): IO[Response] =
    httpClient.use(_.expect[Response](transformRequest(request))
      .handleErrorWith(e => IO.pure(Response.Error(e.getMessage))))

}
