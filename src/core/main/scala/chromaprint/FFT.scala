package chromaprint

import cats.effect.IO
import fs2.{Chunk, Pipe}

trait FFT {

  def pipe(frameLength: Int): Pipe[IO, Chunk[Double], IndexedSeq[Double]]

}
