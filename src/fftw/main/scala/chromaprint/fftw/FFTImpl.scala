package chromaprint.fftw

import cats.effect.IO
import chromaprint.{FFTAbstract, discard}
import fs2.{Chunk, Pipe, Stream}
import org.bytedeco.javacpp.fftw3._
import org.bytedeco.javacpp.{DoublePointer, Loader, fftw3}

object FFTImpl extends FFTAbstract {

  type TFrame = Array[Double]
  type TComplex = (Double, Double)

  final class Context(val frameLength: Int) {
    discard(Loader.load(classOf[fftw3]))
    val signal: DoublePointer = new DoublePointer(frameLength * 2)
    val output: DoublePointer = new DoublePointer(frameLength * 2)
    val plan: fftw3.fftw_plan = fftw_plan_r2r_1d(frameLength, signal, output, FFTW_FORWARD, FFTW_ESTIMATE.toInt)
  }

  def pipe(frameLength: Int): Pipe[IO, Chunk[Double], Vector[Double]] =
    input =>
      Stream.bracket(IO {
        new Context(frameLength)
      })(
        ctx => IO {
          fftw_destroy_plan(ctx.plan)
        }
      ).map(pipeCtx(_)(input)).flatten

  private def pipeCtx(ctx: Context): Pipe[IO,Chunk[Double],Vector[Double]] =
    _.map{ inputFrame: Chunk[Double] =>
      val plan = ctx.plan
      discard(ctx.signal.put(inputFrame.toArray, 0, ctx.frameLength))
      fftw_execute(plan)
      val resultArray = new Array[Double](ctx.frameLength * 2)
      discard(ctx.output.get(resultArray))
      resultArray
    }.map(transformFrame(_, ctx.frameLength))

  def frameIndex(frame: TFrame, length: Int, i: Int): TComplex =
    (frame(i), frame(length - i))

  def complexToReal(complex: TComplex): Double =
    complex._1

  def complexToImag(complex: TComplex): Double =
    complex._2

}
