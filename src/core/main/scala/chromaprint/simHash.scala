package chromaprint

import spire.math.UInt

object simHash {

  val length: Int = 32

  def apply(data: Seq[UInt]): UInt =
    data.foldLeft(Vector.fill[Int](length)(0)) {
      (hash, el) =>
        (0 until length).foldLeft(hash) {
          (iHash, i) =>
            iHash.updated(
              i,
              iHash(i) + (
                if ((el & (UInt(1) << i)).toInt == 0) {
                  -1
                } else {
                  1
                })
            )
        }
    }.zipWithIndex.foldLeft(UInt(0)){
      (result, next) =>
        if (next._1 > 0) {
          result | (UInt(1) << next._2)
        } else {
          result
        }
    }
}
