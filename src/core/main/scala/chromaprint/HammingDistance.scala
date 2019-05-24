package chromaprint

import spire.math.UInt

object HammingDistance {

  def apply(a: UInt, b: UInt): Int =
    countSetBits(a ^ b)

  // https://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel
  def countSetBits(v: UInt): Int = {
    val n32: UInt = ~UInt(0)
    val c1: UInt = v - ((v >> 1) & n32 / UInt(3))
    val c2: UInt = (c1 & n32 / UInt(15) * UInt(3)) + ((c1 >> 2) & n32 / UInt(15) * UInt(3))
    val c3: UInt = (c2 + (c2 >> 4)) & n32 / UInt(255) * UInt(15)
    ((c3 * (n32 / UInt(255))) >> 3 * 8).toInt
  }

}
