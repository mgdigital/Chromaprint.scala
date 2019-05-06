package chromaprint.core

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

  def normalizeValues
  (
    values: Seq[Double],
    norm: Double,
    threshold: Double = 0.01
  ): Seq[Double] =
    if (norm < threshold) {
      Seq.fill[Double](values.length)(0D)
    } else {
      values.map(_ / norm)
    }
}
