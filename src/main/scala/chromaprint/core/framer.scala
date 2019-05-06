package chromaprint.core

import scala.math.{cos,Pi}

object framer {

  def hammingWindow(size: Int): Vector[Double] =
    (0 until size).toVector.map { i =>
      0.54 - 0.46 * cos(i.toDouble * 2D * Pi / (size - 1))
    }

  def scaledHammingWindow(size: Int, scale: Double): Vector[Double] =
    hammingWindow(size).map(_ * scale)

  def shortHammingWindow(size: Int): Vector[Double] =
    scaledHammingWindow(size, 1D / Short.MaxValue)

  def applyHammingWindow(window: Vector[Double], input: Seq[Short]): Vector[Double] = {
    require(window.length >= input.length)
    input.toVector.zip(window).map(e => e._1.toDouble * e._2)
  }

  final case class Config
  (
    frameSize: Int,
    overlap: Int
  ) {

    val step: Int =
      frameSize - overlap

    val window: Vector[Double] =
      shortHammingWindow(frameSize)
  }

  var c: Int = 0

  def apply(config: Config, input: Seq[Short]): Seq[Vector[Double]] =
    input.sliding(config.frameSize, config.step)
      .toStream
      .takeWhile(_.length == config.frameSize)
      .map(applyHammingWindow(config.window, _))
}
