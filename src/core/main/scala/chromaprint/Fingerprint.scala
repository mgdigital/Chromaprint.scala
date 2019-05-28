package chromaprint

import spire.math.UInt

final case class Fingerprint
(
  algorithm: Int,
  trackDuration: Float,
  data: IndexedSeq[UInt]
) {

  lazy val length: Int =
    data.length

  lazy val compressed: String =
    FingerprintCompressor(algorithm, data)

  lazy val hash: UInt =
    SimHash(data)

  def append(next: UInt): Fingerprint =
    copy(data = data :+ next)

  def formatted(raw: Boolean): String =
    s"DURATION=${trackDuration.toInt}\nFINGERPRINT=${if (raw) data.mkString(",") else compressed}\n"

  override def toString: String =
    formatted(false)
}
