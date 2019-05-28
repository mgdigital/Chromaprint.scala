package chromaprint

import fs2.{Chunk, Pipe}

import scala.math.{Pi, cos}

object HammingWindow {

  def sized(size: Int): IndexedSeq[Double] =
    (0 until size).toVector map (i => 0.54 - 0.46 * cos(i.toDouble * 2D * Pi / (size - 1)))

  def scaled(size: Int, scale: Double): IndexedSeq[Double] =
    sized(size) map (_ * scale)

  def short(size: Int): IndexedSeq[Double] =
    scaled(size, 1D / Short.MaxValue)

  def pipe[F[_]](window: IndexedSeq[Double]): Pipe[F,IndexedSeq[Short],Chunk[Double]] =
    _.map(f => Chunk.indexedSeq[Double](f.indices.map(i => f(i).toDouble * window(i))))
}
