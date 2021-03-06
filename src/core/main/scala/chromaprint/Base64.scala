package chromaprint

import scodec.bits.{Bases, ByteVector}

object Base64 {

  final class EncoderException(message: String) extends Exception(message)

  val alphabet: Bases.Base64Alphabet = Bases.Alphabets.Base64Url

  def apply(data: Seq[Byte]): String =
    apply(ByteVector(data))

  def apply(data: ByteVector): String =
    data.toBase64(alphabet).
      replaceAll("=+$", "")

  def unapply(str: String): Option[IndexedSeq[Byte]] =
    decode(str) match {
      case Left(_) =>
        None
      case Right(bytes) =>
        Some(bytes)
    }

  def decode(str: String): Either[EncoderException,IndexedSeq[Byte]] =
    ByteVector.fromBase64Descriptive(str, alphabet) match {
      case Right(bv) =>
        Right(bv.toArray.toIndexedSeq)
      case Left(err) =>
        Left(new EncoderException(err))
    }

}
