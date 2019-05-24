package chromaprint

import fs2.{Chunk, Pipe}

abstract class FFTAbstract extends FFT {

  type TFrame
  type TComplex

  def transformFrame(frame: TFrame, length: Int): Vector[Double] =
    Vector(complexToRealSquared(frameIndex(frame, length, 0))) ++
      (1 until (length / 2)).map(frameIndex(frame, length, _)).map(complexToSquared) ++
      Vector(complexToImagSquared(frameIndex(frame, length, length / 2)))

  def transformResult[F[_]](length: Int): Pipe[F,TFrame,Vector[Double]] =
    _ map (transformFrame(_, length))

  def complexToSquared(complex: TComplex): Double =
    complexToRealSquared(complex) + complexToImagSquared(complex)

  def complexToRealSquared(complex: TComplex): Double = {
    val real = complexToReal(complex)
    real * real
  }

  def complexToImagSquared(complex: TComplex): Double = {
    val imag = complexToImag(complex)
    imag * imag
  }

  def frameIndex(frame: TFrame, length: Int, i: Int): TComplex

  def complexToReal(complex: TComplex): Double

  def complexToImag(complex: TComplex): Double

}
