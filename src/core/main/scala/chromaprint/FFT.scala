package chromaprint

import fs2.{Pipe, Pure, Stream}

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import duration._

object FFT {

  final case class Complex(real: Double, imag: Double) {

    lazy val realSquared: Double =
      real * real

    lazy val imagSquared: Double =
      imag * imag

    lazy val squared: Double =
      realSquared + imagSquared
  }

  def transformFrame(frame: Vector[Complex]): Vector[Double] =
    Vector(frame(0).realSquared) ++
      (1 until (frame.length / 2)).map(frame(_).squared) ++
      Vector(frame(frame.length / 2).imagSquared)

  def transformResult[F[_]]: Pipe[F,Vector[Complex],Vector[Double]] =
    _ map transformFrame
}

trait FFT {

  import FFT._

  def computeFrame(frame: Vector[Double]): Vector[Complex]

  def pipe[F[_]]: Pipe[F, Vector[Double], Vector[Double]] =
    _ map computeFrame through transformResult

}
