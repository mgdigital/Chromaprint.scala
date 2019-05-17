package chromaprint

import fs2.Pipe

object chromaFilter {

  val coefficients: Vector[Double] =
    Vector(
      0.25,
      0.75,
      1.0,
      0.75,
      0.25
    )

  val defaultParams: Vector[Double] = coefficients

  val bufferLength = 8

  def pipe[F[_]]: Pipe[F,Vector[Double],Vector[Double]] =
    pipe(coefficients)

  def pipe[F[_]](coefficients: Vector[Double]): Pipe[F,Vector[Double],Vector[Double]] = {
    var buffer: Vector[Vector[Double]] = Vector.fill[Vector[Double]](bufferLength)(
      Vector.fill[Double](chroma.numBands)(0)
    )
    var bufferSize: Int = 1
    var bufferOffset: Int = 0

    _.map(fs => {
      buffer = buffer.updated(bufferOffset, fs)
      bufferOffset = (bufferOffset + 1) % bufferLength
      if (bufferSize >= coefficients.length) {
        val offset: Int = (bufferOffset + bufferLength - coefficients.length) % bufferLength
        Some((0 until chroma.numBands).foldLeft(Vector.fill[Double](chroma.numBands)(0)) {
          (r, i) =>
            coefficients.indices.foldLeft(r) {
              (rr, j) =>
                rr.updated(
                  i,
                  rr(i) + buffer((offset + j) % bufferLength)(i) * coefficients(j)
                )
            }
        })
      } else {
        bufferSize += 1
        None
      }
    }).unNone
  }

}
