package chromaprint.breeze

import chromaprint.FFT

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object FFTImplAsync extends FFTImpl {

  import FFT._

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  val timeout: Duration = 30.seconds

  val grouping: Int = 100

  def computeFrames(input: Seq[Vector[Double]]): Seq[Vector[Complex]] =
    Await.result(
      Future.sequence(input.grouped(grouping).map(frames => Future { frames.map(computeFrame) })),
      atMost = timeout
    ).foldLeft(Stream.empty[Vector[Complex]]){ (s, n) => s ++ n}

}
