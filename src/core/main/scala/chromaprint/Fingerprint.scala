package chromaprint

import spire.math.UInt

object Fingerprint {

  def apply
  (
    algorithm: Int,
    trackDuration: Int,
    data: Vector[UInt]
  ): Fingerprint =
    Fingerprint(algorithm, trackDuration.toFloat, data)

  def apply
  (
    algorithm: Int,
    trackDuration: Float,
    data: Vector[UInt]
  ): Fingerprint =
    Fingerprint(algorithm, Some(trackDuration), data)
}

final case class Fingerprint
(
  algorithm: Int,
  trackDuration: Option[Float],
  data: Vector[UInt]
) {

  def withTrackDuration(duration: Float): Fingerprint =
    copy(trackDuration = Some(duration))

  def withTrackDuration(duration: Int): Fingerprint =
    withTrackDuration(duration.toFloat)

  def clearTrackDuration: Fingerprint =
    copy(trackDuration = None)

  lazy val hasTrackDuration: Boolean =
    trackDuration.isDefined

  lazy val length: Int =
    data.length

  lazy val sampledDuration: Float =
    length.toFloat / Config.Defaults.frameSize

  lazy val duration: Float =
    trackDuration.getOrElse(sampledDuration)

  lazy val compressed: String =
    fingerprintCompressor(algorithm, data)

  lazy val hash: UInt =
    simHash(data)

  def formatted(raw: Boolean): String =
    s"DURATION=${duration.toInt}\nFINGERPRINT=${if (raw) data.mkString(",") else compressed}\n"

  override def toString: String =
    formatted(false)
}
