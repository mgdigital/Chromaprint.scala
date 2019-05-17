package chromaprint

import spire.math.UInt

final case class Fingerprint
(
  algorithm: Int,
  trackDuration: Float,
  data: Vector[UInt]
) {

  lazy val length: Int =
    data.length

  lazy val compressed: String =
    fingerprintCompressor(algorithm, data)

  lazy val hash: UInt =
    simHash(data)

  def append(next: UInt): Fingerprint =
    copy(data = data :+ next)

  def formatted(raw: Boolean): String =
    s"DURATION=${trackDuration.toInt}\nFINGERPRINT=${if (raw) data.mkString(",") else compressed}\n"

  override def toString: String =
    formatted(false)
}
