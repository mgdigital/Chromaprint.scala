package chromaprint

import spire.math.UInt

import scala.io.Source

object TestHelper {

  def hammingDistance(a: UInt, b: UInt): Int =
    countSetBits(a ^ b)

  // https://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel
  def countSetBits(v: UInt): Int = {
    val n32: UInt = ~UInt(0)
    var c: UInt = v - ((v >> 1) & n32 / UInt(3))
    c = (c & n32 / UInt(15) * UInt(3)) + ((c >> 2) & n32 / UInt(15) * UInt(3))
    c = (c + (c >> 4)) & n32 / UInt(255) * UInt(15)
    ((c * (n32 / UInt(255))) >> 3 * 8).toInt
  }

  def fpcalcOutToBody(output: String): Option[String] =
    output.split('\n').find(_.startsWith("FINGERPRINT=")).map(_.drop(12).trim)

  lazy val fpcalcOut: String =
    Source.fromURL(getClass.getResource("/fpcalc.out")).mkString

  lazy val fpcalcRawOut: String =
    Source.fromURL(getClass.getResource("/fpcalc-raw.out")).mkString

  lazy val fpcalcBody: String =
    fpcalcOutToBody(fpcalcOut).get

  lazy val fpcalcRawBody: String =
    fpcalcOutToBody(fpcalcRawOut).get

  lazy val fpcalcRawData: Vector[UInt] =
    fpcalcRawBody.split(',').toVector.map(_.toLong).map(UInt(_))
}
