package object chromaprint {

  implicit class Pipe[T](val v: T) extends AnyVal {
    def |>[U] (f: T => U): U = f(v)
  }

  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _: Any = evaluateForSideEffectOnly
    () //Return unit to prevent warning due to discarding value
  }
}
