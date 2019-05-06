package chromaprint.breeze

import breeze.linalg.DenseVector
import breeze.signal.fourierTr
import chromaprint.core.{FFT => CoreFFT}

object FFT extends CoreFFT {

  import CoreFFT._

  def computeFrames(input: Seq[Vector[Double]]): Seq[Vector[Complex]] =
    input.map(computeFrame)

  def computeFrame(frame: Vector[Double]): Vector[Complex] =
    fourierTr(
      DenseVector(
        frame.toArray
      )
    ).toScalaVector
      .map(c => Complex(c.real, c.imag))

}
