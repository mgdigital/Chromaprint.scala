package chromaprint.acoustid

sealed trait Response {

  def isOK: Boolean

  def results: Vector[Metadata.Result]

  def nonEmptyResults: Vector[Metadata.NonEmptyResult]

  def topResult: Option[Metadata.NonEmptyResult] =
    nonEmptyResults.headOption
}

object Response {

  final case class OK(results: Vector[Metadata.Result]) extends Response {

    def isOK: Boolean = true

    lazy val nonEmptyResults: Vector[Metadata.NonEmptyResult] = results.flatMap{
      case r: Metadata.NonEmptyResult =>
        Some(r)
      case _ =>
        None
    }

    lazy val isEmpty: Boolean = nonEmptyResults.isEmpty
  }

  final case class Error(message: String) extends Response {

    def isOK: Boolean = false

    val results, nonEmptyResults = Vector.empty
  }
}
