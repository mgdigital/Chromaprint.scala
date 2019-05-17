package chromaprint.acoustid

import chromaprint.Fingerprint

import com.softwaremill.sttp.Uri
import com.softwaremill.sttp.quick._

object lookup {

  val defaultBaseUrl: Uri =
    uri"https://api.acoustid.org/v2/lookup"

  val clientIdEnvVar: String =
    "ACOUSTID_CLIENT_ID"

  def clientId: Option[String] =
    System.getenv(clientIdEnvVar) match {
      case str: String if str != "" =>
        Some(str)
      case _ =>
        None
    }

  object Config {

    def apply
    (
      clientId: String
    ): Config =
      new Config(Some(clientId), defaultBaseUrl)

    val default: Config =
      new Config(None, defaultBaseUrl)

  }

  final case class Config
  (
    clientId: Option[String],
    baseUrl: Uri
  ) {

    def withClientId(clientId: String): Config =
      copy(clientId = Some(clientId))
  }

  object Params {

    val defaultMeta: Set[metadata.MetadataGroup] = Set(
      metadata.recordings
    )

    def apply(fingerprint: Fingerprint): Params =
      Params(fingerprint, defaultMeta)
  }

  final case class Params
  (
    fingerprint: Fingerprint,
    meta: Set[metadata.MetadataGroup]
  )

  def url(config: Config, params: Params): Uri = {
    var url: Uri = config.baseUrl
    config.clientId.foreach { clientId => url = url.param("client", clientId) }
    url.param("meta", params.meta.mkString(" "))
      .param("duration", params.fingerprint.trackDuration.toInt.toString)
      .param("fingerprint", params.fingerprint.compressed)
  }

  object Response {
    import play.api.libs.json._
    implicit val artistReads: Reads[metadata.Artist] = Json.reads[metadata.Artist]
    implicit val recordingReads: Reads[metadata.Recording] = Json.reads[metadata.Recording]
    implicit val resultReads: Reads[metadata.Result] = Json.reads[metadata.Result]
    implicit val responseReads: Reads[OKResponse] = Json.reads[OKResponse]
      .preprocess{
        case responseObj @ JsObject(_) =>
          (responseObj \ "results").asOpt[JsArray] match {
            case Some(arr) =>
              responseObj + ("results" -> JsArray(arr.value.filter{
                case resultObj @ JsObject(_) =>
                  resultObj.keys.contains("recordings")
                case _ =>
                  true
              }))
            case _ =>
              responseObj
          }
        case o =>
          o
      }

    def parse(json: String): Either[LookupException,Response] =
      Json.fromJson[OKResponse](Json.parse(json)) match {
        case JsSuccess(response, _) =>
          Right(response)
        case e: JsError =>
          Left(new LookupException("JSON error: " +
            e.errors.map(e => e._1.path.mkString("/") + ": " + e._2.map(_.message).mkString("; ")).mkString("; "))
          )
      }
  }

  sealed trait Response

  final case class OKResponse(results: Vector[metadata.Result]) extends Response
  final case class ErrorResponse(message: String) extends Response

  class LookupException(message: String) extends Exception(message)

  def apply(config: Config, fingerprint: Fingerprint): Either[LookupException,Response] =
    apply(config, Params(fingerprint))

  def apply(config: Config, params: Params): Either[LookupException,Response] = {
    val uri = url(config, params)
    sttp.get(uri).send().body match {
      case Right(json) =>
        Response.parse(json)
      case Left(e) =>
        Left(new LookupException(e))

    }
  }
}
