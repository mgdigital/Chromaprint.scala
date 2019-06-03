package chromaprint.acoustid

import org.http4s._

object Config {

  val defaultBaseUri =
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

  def apply
  (
    clientId: String
  ): Config =
    new Config(Some(clientId), defaultBaseUri)

  val default: Config =
    new Config(None, defaultBaseUri)

}

final case class Config
(
  clientId: Option[String],
  baseUri: Uri
) {

  def withClientId(clientId: String): Config =
    copy(clientId = Some(clientId))
}
