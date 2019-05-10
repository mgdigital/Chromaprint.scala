package chromaprint.breeze

import breeze.linalg.DenseVector
import breeze.signal.fourierTr
import chromaprint.FFT

object FFTImpl extends FFTImpl {

  import FFT._

  def computeFrames(input: Seq[Vector[Double]]): Seq[Vector[Complex]] =
    input.map(computeFrame)

}

trait FFTImpl extends FFT {

  import FFT._

  def computeFrame(frame: Vector[Double]): Vector[Complex] =
    fourierTr(
      DenseVector(
        frame.toArray
      )
    ).toScalaVector
      .map(c => Complex(c.real, c.imag))

}
