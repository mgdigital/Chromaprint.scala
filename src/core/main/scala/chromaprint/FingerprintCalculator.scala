package chromaprint

import fs2.Pipe
import spire.math.UInt

object FingerprintCalculator {

  val grayCode: Vector[UInt] =
    Vector(
      0,
      1,
      3,
      2
    ).map(UInt(_))

  def pipe[F[_]](config: Classifier.Config): Pipe[F,Vector[Double],UInt] =
    _.mapAccumulate[Vector[Vector[Double]],Option[UInt]](Vector.empty[Vector[Double]]){
      (rows, thisRow) =>
        val newRows = (rows :+ thisRow) takeRight config.maxFilterSpan
        val sub = if (newRows.length < config.maxFilterWidth) {
          None
        } else {
          Some(subFingerprint(config, newRows, newRows.length - config.maxFilterWidth))
        }
        (newRows, sub)
    }.map(_._2).unNone

  def subFingerprint
  (
    config: Classifier.Config,
    integral: Vector[Vector[Double]],
    offset: Int
  ): UInt =
    config.classifiers.foldLeft(UInt(0)){
      (bits, c) =>
        (bits << 2) | grayCode(c(integral, offset))
    }
}
