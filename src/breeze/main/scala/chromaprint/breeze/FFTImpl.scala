package chromaprint.breeze

import breeze.linalg.DenseVector
import breeze.signal.fourierTr
import chromaprint.FFT

object FFTImpl extends FFT {

  import FFT._

  def computeFrame(frame: Vector[Double]): Vector[Complex] =
    fourierTr(
      DenseVector(
        frame.toArray
      )
    ).toScalaVector
      .map(c => Complex(c.real, c.imag))

}
