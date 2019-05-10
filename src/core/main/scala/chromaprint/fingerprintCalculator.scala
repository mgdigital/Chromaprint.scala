package chromaprint

import spire.math.UInt

object fingerprintCalculator {

  val grayCode: Vector[UInt] =
    Vector(
      0,
      1,
      3,
      2
    ).map(UInt(_))

  def apply(config: Classifier.Config, integral: Image): Vector[UInt] =
    (0 until integral.rows - config.maxFilterWidth + 1)
      .map(subFingerprint(config, integral, _))
      .toVector

  def subFingerprint
  (
    config: Classifier.Config,
    integral: Image,
    offset: Int
  ): UInt =
    config.classifiers.foldLeft(UInt(0)){
      (bits, c) =>
        (bits << 2) | grayCode(c(integral, offset))
    }
}
