package chromaprint

import fs2.{Pure,Stream}
import scala.math.{Pi, sin, sqrt}

abstract class AbstractFFTSpec extends AbstractSpec {

  behavior of "FFT"

  def fftImpl: FFT

  val nFrames = 3

  val frameSize = 32
  val overlap = 8

  val framerParams = framer.Config(frameSize, overlap)

  def audioToFFTFrames(input: Stream[Pure,Short]): Vector[Vector[Double]] =
    input
      .through(framer.pipe(framerParams))
      .through(hammingWindow.pipe(frameSize))
      .through(fftImpl.pipe).compile.toVector

  val inputSize: Int = frameSize + (nFrames - 1) * (frameSize - overlap) // 80

  it should "sine" in {

    val sampleRate = 1000

    val freq: Double = 7 * sampleRate / frameSize // 218.0

    val input: Vector[Short] = (0 until inputSize).toVector.map{i =>
      (Short.MaxValue * sin(i.toDouble * freq * 2D * Pi / sampleRate)).toShort
    }
    val expectedSpectrum: Vector[Double] = Vector(
      2.87005e-05,
      0.00011901,
      0.00029869,
      0.000667172,
      0.00166813,
      0.00605612,
      0.228737,
      0.494486,
      0.210444,
      0.00385322,
      0.00194379,
      0.00124616,
      0.000903851,
      0.000715237,
      0.000605707,
      0.000551375,
      0.000534304
    )
    val transformed = audioToFFTFrames(Stream(input :_*))

    transformed should have length nFrames
    transformed.foreach { frame =>
      frame should have length (frameSize / 2 + 1)
      frame.indices foreach { i =>
        val magnitude = sqrt(frame(i)) / frame.length
        magnitude should be (expectedSpectrum(i) +- 0.001)
      }
    }
  }

  it should "dc" in {
    val input: Vector[Short] = (0 until inputSize).toVector
      .map(_ => (Short.MaxValue * 0.5).toShort)
    val expectedSpectrum: Vector[Double] = Vector(
      0.494691,
      0.219547,
      0.00488079,
      0.00178991,
      0.000939219,
      0.000576082,
      0.000385808,
      0.000272904,
      0.000199905,
      0.000149572,
      0.000112947,
      8.5041e-05,
      6.28312e-05,
      4.4391e-05,
      2.83757e-05,
      1.38507e-05,
      0
    )
    val transformed = audioToFFTFrames(Stream(input :_*))

    transformed should have length nFrames
    transformed.foreach { frame =>
      frame should have length (frameSize / 2 + 1)
      frame.indices foreach { i =>
        val magnitude = sqrt(frame(i)) / frame.length
        magnitude should be (expectedSpectrum(i) +- 0.001)
      }
    }
  }
}
