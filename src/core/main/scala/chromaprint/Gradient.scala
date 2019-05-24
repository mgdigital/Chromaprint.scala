package chromaprint

object Gradient {

  def apply(input: IndexedSeq[Float]): IndexedSeq[Float] = {
    var grad: IndexedSeq[Float] = IndexedSeq.empty
    var i: Int = 0

    def done: Boolean =
      i >= input.length

    def read(): Float = {
      assert(!done)
      val v = input(i)
      i += 1
      v
    }

    def append(value: Float): Unit =
      grad :+= value

    if (!done) {
      var f0: Float = read()
      if (done) {
        append(0F)
      } else {
        var f1: Float = read()
        append(f1 - f0)
        if (done) {
          append(f1 - f0)
        } else {
          var f2: Float = 0F
          do {
            f2 = read()
            append((f2 - f0) / 2)
            f0 = f1
            f1 = f2
          } while (!done)
          append(f2 - f0)
        }
      }
    }
    grad
  }
}
