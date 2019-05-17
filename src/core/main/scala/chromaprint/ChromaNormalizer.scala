package chromaprint

import fs2.Pipe

import scala.math.sqrt

object ChromaNormalizer {

  def pipe[F[_]]: Pipe[F,Vector[Double],Vector[Double]] =
    _ map normalizeValues

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

  def normalizeValues(values: Vector[Double]): Vector[Double] =
    normalizeValues(values, euclidianNorm(values), defaultThreshold)

  def normalizeValues
  (
    values: Vector[Double],
    norm: Double,
    threshold: Double
  ): Vector[Double] =
    if (norm < threshold) {
      Vector.fill[Double](values.length)(0D)
    } else {
      values.map(_ / norm)
    }
}
