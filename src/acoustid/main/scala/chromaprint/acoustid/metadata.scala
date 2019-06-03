package chromaprint.acoustid

object Metadata {

  trait MetadataEntry {
    def id: String
  }

  sealed trait MetadataGroup {
    lazy val name: String = this.getClass.getSimpleName
    type TEntry <: MetadataEntry
  }

  case object recordings extends MetadataGroup {
    type TEntry = Recording
  }

  sealed trait BaseResult extends MetadataEntry {
    def id: String
    def score: Float
  }

  final case class EmptyResult
  (
    id: String,
    score: Float
  ) extends BaseResult

  final case class Result
  (
    id: String,
    score: Float,
    recordings: Vector[Recording]
  ) extends BaseResult

  case class Recording
  (
    id: String,
    duration: Option[Int],
    title: String,
    artists: Vector[Artist]
  ) extends MetadataEntry

  case class Artist
  (
    id: String,
    name: String
  ) extends MetadataEntry
}
