package chromaprint.acoustid

object metadata {

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

  case class Result
  (
    id: String,
    score: Float,
    recordings: Vector[Recording]
  ) extends MetadataEntry

  case class Recording
  (
    id: String,
    duration: Int,
    title: String,
    artists: Vector[Artist]
  ) extends MetadataEntry

  case class Artist
  (
    id: String,
    name: String
  ) extends MetadataEntry

}
