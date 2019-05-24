package chromaprint.breeze

import breeze.linalg.DenseVector
import breeze.math.Complex
import breeze.signal.fourierTr
import chromaprint.FFTParEval
import fs2.Chunk

object FFTImpl extends FFTParEval {

  type TFrame = DenseVector[Complex]
  type TComplex = Complex

  def computeFrame(frame: Chunk[Double]): TFrame =
    fourierTr(
      DenseVector(
        frame.toArray
      )
    )

  def frameIndex(frame: TFrame, length: Int, i: Int): TComplex =
    frame(i)

  def complexToReal(complex: TComplex): Double =
    complex.real

  def complexToImag(complex: TComplex): Double =
    complex.imag
}
