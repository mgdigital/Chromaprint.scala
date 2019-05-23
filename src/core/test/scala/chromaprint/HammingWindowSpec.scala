package chromaprint

import fs2.{Chunk, Pure, Stream}

class HammingWindowSpec extends AbstractSpec {

  behavior of "Hamming window"

  val expected: Vector[Double] = Vector(
    0.08,
    0.187619556165,
    0.460121838273,
    0.77,
    0.972258605562,
    0.972258605562,
    0.77,
    0.460121838273,
    0.187619556165,
    0.08
  )

  val precision: Double = 0.000000000001

  it should "prepare Hamming window" in {
    val window = HammingWindow.sized(10)

    window should have length expected.length
    expected.indices.foreach { i =>
      window(i) should be (expected(i) +- precision)
    }
  }

  it should "apply Hamming window" in {
    val input = Stream[Pure,Vector[Short]]((0 until 10).map(_ => Short.MaxValue).toVector)
    val window = HammingWindow.short(10)
    val output: Vector[Chunk[Double]] = input.through[Pure,Chunk[Double]](HammingWindow.pipe(window)).compile.toVector

    output should have length 1
    output(0).toVector should have length 10
    expected.indices.foreach { i =>
      output(0)(i) should be (expected(i) +- precision)
    }
  }
}
