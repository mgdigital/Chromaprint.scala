package chromaprint

import fs2.Pipe

import scala.math.{Pi, cos}

object HammingWindow {

  def sized(size: Int): Vector[Double] =
    (0 until size).toVector map (i => 0.54 - 0.46 * cos(i.toDouble * 2D * Pi / (size - 1)))

  def scaled(size: Int, scale: Double): Vector[Double] =
    sized(size) map (_ * scale)

  def short(size: Int): Vector[Double] =
    scaled(size, 1D / Short.MaxValue)

  def pipe[F[_]](frameSize: Int): Pipe[F,Seq[Short],Vector[Double]] =
    pipe(short(frameSize))

  def pipe[F[_]](window: Vector[Double]): Pipe[F,Seq[Short],Vector[Double]] =
    _ map (_.zip(window) map (e => e._1.toDouble * e._2)) map (_.toVector)
}
