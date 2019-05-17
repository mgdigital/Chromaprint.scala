package object chromaprint {

  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _: Any = evaluateForSideEffectOnly
    () //Return unit to prevent warning due to discarding value
  }
}
