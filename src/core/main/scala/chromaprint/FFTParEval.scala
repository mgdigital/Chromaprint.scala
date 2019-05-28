package chromaprint

import cats.effect.{ContextShift, IO}
import fs2.{Chunk, Pipe, Stream}

import scala.concurrent.ExecutionContext

trait FFTParEval extends FFTAbstract {

  val chunkSize: Int = 50
  val maxConcurrent: Int = 5

  implicit val executionContext: ExecutionContext = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  def pipe(length: Int): Pipe[IO, Chunk[Double], IndexedSeq[Double]] =
    _.chunkN(chunkSize).
      parEvalMap(maxConcurrent)(chunk => IO (
        Stream.chunk[IO,IndexedSeq[Double]](
          chunk.map(f => transformFrame(computeFrame(f), length))
        )
      )).flatten

  def computeFrame(frame: Chunk[Double]): TFrame

}
