package chromaprint

import scodec.bits.{Bases, ByteVector}

object Base64 {

  final class EncoderException(message: String) extends Exception(message)

  private val alphabet: Bases.Base64Alphabet = Bases.Alphabets.Base64Url

  def apply(data: Seq[Byte]): String =
    ByteVector(data).toBase64(alphabet)
      .replaceAll("=+$", "")

  def unapply(str: String): Option[Vector[Byte]] =
    decode(str) match {
      case Left(e) =>
        None
      case Right(bytes) =>
        Some(bytes)
    }

  def decode(str: String): Either[EncoderException,Vector[Byte]] =
    ByteVector.fromBase64Descriptive(str, alphabet) match {
      case Right(bv) =>
        Right(bv.toArray.toVector)
      case Left(err) =>
        Left(new EncoderException(err))
    }

}
