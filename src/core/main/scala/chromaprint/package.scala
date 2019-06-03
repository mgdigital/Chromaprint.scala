import scala.io.Source

package object chromaprint {

  lazy val version: String =
    Source.fromURL(getClass.getResource("/version.properties")).
      mkString.
      split("^version=", 2).
      lift(1).
      getOrElse{throw new RuntimeException("Cannot parse version number")}

  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _: Any = evaluateForSideEffectOnly
    () //Return unit to prevent warning due to discarding value
  }

}
