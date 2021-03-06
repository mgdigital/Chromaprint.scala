import scala.io.Source

package object chromaprint {

  lazy val version: String = {
    val source = Source.fromURL(getClass.getResource("/version.properties"))
    val version = source.mkString
      .split("^version=", 2)
      .lift(1)
      .getOrElse{throw new RuntimeException("Cannot parse version number")}
    source.close()
    version
  }

  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _: Any = evaluateForSideEffectOnly
    () //Return unit to prevent warning due to discarding value
  }

}
