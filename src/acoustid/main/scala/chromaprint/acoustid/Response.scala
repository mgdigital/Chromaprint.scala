package chromaprint.acoustid

sealed trait Response {

  def isOK: Boolean
}

object Response {

  final case class OK(results: Vector[Metadata.BaseResult]) extends Response {

    def isOK: Boolean = true

    lazy val nonEmptyResults: Vector[Metadata.Result] = results.flatMap{
      case r: Metadata.Result =>
        Some(r)
      case _ =>
        None
    }

    lazy val isEmpty: Boolean = nonEmptyResults.isEmpty
  }

  final case class Error(message: String) extends Response {

    def isOK: Boolean = false
  }
}
