package chromaprint.acoustid

import io.circe.generic.auto._
import io.circe.generic.semiauto._

object Decoder {

  import io.circe.Decoder

  implicit val decodeNonEmptyResult: Decoder[Metadata.Result] = deriveDecoder[Metadata.Result]

  implicit val decodeEmptyResult: Decoder[Metadata.EmptyResult] = deriveDecoder[Metadata.EmptyResult]

  implicit val decodeResult: Decoder[Metadata.BaseResult] = decodeNonEmptyResult.either(decodeEmptyResult).map{
    case Left(r) =>
      r
    case Right(r) =>
      r
  }

  implicit val decodeOKResponse: Decoder[Response.OK] = deriveDecoder[Response.OK]

  implicit val decodeErrorResponse: Decoder[Response.Error] = deriveDecoder[Response.Error]

  implicit val decodeResponse: Decoder[Response] = decodeOKResponse.either(decodeErrorResponse).map{
    case Left(r) =>
      r
    case Right(r) =>
      r
  }
}
