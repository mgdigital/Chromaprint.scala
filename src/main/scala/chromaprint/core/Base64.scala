package chromaprint.core

object Base64 {

  final class EncoderException(message: String) extends Exception(message)

  private val encoder: java.util.Base64.Encoder = java.util.Base64.getUrlEncoder
  private val decoder: java.util.Base64.Decoder = java.util.Base64.getUrlDecoder

  def apply(data: Seq[Byte]): String =
    encoder.encodeToString(data.toArray)
      .replaceAll("=+$", "")

  def unapply(str: String): Option[Vector[Byte]] =
    decode(str).toOption

  def decode(str: String): Either[EncoderException,Vector[Byte]] =
    try {
      Right(decoder.decode(str).toVector)
    } catch {
      case e: IllegalArgumentException =>
        Left(new EncoderException(e.getMessage))
    }
}
