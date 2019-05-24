package chromaprint

import scala.math.sqrt

object GaussianFilter {

  object ReflectIterator {

    def apply(size: Int): ReflectIterator =
      new ReflectIterator(size, 0, true)
  }

  final case class ReflectIterator private (size: Int, pos: Int, forward: Boolean) {

    require(size >= 0)
    assert(pos >= 0)
    assert(pos < size)

    def moveForward: ReflectIterator =
      if (forward) {
        if (pos + 1 == size) {
          copy(forward = false)
        } else {
          copy(pos = pos + 1)
        }
      } else {
        if (pos == 0) {
          copy(forward = true)
        } else {
          copy(pos = pos - 1)
        }
      }

    def turn: ReflectIterator =
      copy(forward = !forward)

    def moveBack: ReflectIterator =
      turn.moveForward.turn

    def forwardDistance: Int =
      if (forward) {
        size - pos - 1
      } else {
        0
      }
  }

  def boxFilter(w: Int, input: IndexedSeq[Float]): IndexedSeq[Float] =
    if (w == 0 || input.isEmpty) {
      IndexedSeq.empty
    } else {
      val wl = w / 2
      val wr = w - wl + 1
      val wx = input.length - w - 1

      var sum: Float = 0F

      var it1: ReflectIterator = ReflectIterator(input.length)
      (0 until wl) foreach { _ =>
        it1 = it1.moveBack
      }
      var it2: ReflectIterator = it1
      (0 until w) foreach { _ =>
        sum += input(it2.pos)
        it2 = it2.moveForward
      }

      var output: IndexedSeq[Float] = IndexedSeq.empty

      def iterate(n: Int): Unit =
        (0 until n) foreach { _ =>
          output :+= sum / w
          sum += input(it2.pos) - input(it1.pos)
          it1 = it1.moveForward
          it2 = it2.moveForward
        }

      if (input.length > w) {
        iterate(wl)
        iterate(wx)
        iterate(wr)
      } else {
        iterate(input.length)
      }

      output
    }

  def apply(sigma: Double, n: Int, input: IndexedSeq[Float]): IndexedSeq[Float] = {
    val w = sqrt(12 * sigma * sigma / n + 1).floor.toInt
    val wl = w - (if (w % 2 == 0) 1 else 0)
    val wu = wl + 2
    val m = ((12 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4)).round.toInt

    var data1: IndexedSeq[Float] = input
    var data2: IndexedSeq[Float] = IndexedSeq.empty

    var i: Int = 0

    def iterate(j: Int, w: Int): Unit =
      while (i < j) {
        val next = boxFilter(w, data1)
        data2 = data1
        data1 = next
        i += 1
      }

    iterate(m, wl)
    iterate(n, wu)

    data1
  }

}
