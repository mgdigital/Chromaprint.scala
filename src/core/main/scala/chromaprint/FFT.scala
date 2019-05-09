package chromaprint

object FFT {

  final case class Complex(real: Double, imag: Double) {

    lazy val realSquared: Double =
      real * real

    lazy val imagSquared: Double =
      imag * imag

    lazy val squared: Double =
      realSquared + imagSquared
  }
}

trait FFT {

  import FFT._

  def computeFrames(input: Seq[Vector[Double]]): Seq[Vector[Complex]]

  final def transform(resultFrame: Vector[Complex]): Vector[Double] =
    Vector(resultFrame(0).realSquared) ++
      (1 until (resultFrame.length / 2)).map(resultFrame(_).squared) ++
      Vector(resultFrame(resultFrame.length / 2).imagSquared)

  final def apply(input: Seq[Vector[Double]]): Seq[Vector[Double]] =
    computeFrames(input).map(transform)

}
