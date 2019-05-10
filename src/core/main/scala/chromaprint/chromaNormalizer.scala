package chromaprint

import scala.math.sqrt

object chromaNormalizer {

  def apply(features: Seq[Vector[Double]]): Seq[Vector[Double]] =
    features.map(f =>
      normalizeValues(
        f,
        euclidianNorm(f)
      ).toVector
    )

  def euclidianNorm(values: Seq[Double]): Double =
    values.foldLeft(0D) {
      (sq, next) => sq + (next * next)
    } match {
      case sq if sq > 0 =>
        sqrt(sq)
      case _ =>
        0D
    }

  val defaultThreshold: Double = 0.01

  def normalizeValues
  (
    values: Seq[Double],
    norm: Double
  ): Seq[Double] =
    normalizeValues(values, norm, defaultThreshold)

  def normalizeValues
  (
    values: Seq[Double],
    norm: Double,
    threshold: Double
  ): Seq[Double] =
    if (norm < threshold) {
      Seq.fill[Double](values.length)(0D)
    } else {
      values.map(_ / norm)
    }
}
