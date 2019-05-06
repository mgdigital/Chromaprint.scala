package chromaprint.core

class framerSpec extends AbstractSpec {

  behavior of "Framer"

  val hammingWindow: Vector[Double] = Vector(
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

  val hammingPrecision: Double = 0.000000000001

  it should "prepare Hamming window" in {
    val window = framer.hammingWindow(10)

    window should have length hammingWindow.length
    hammingWindow.indices.foreach { i =>
      window(i) should be (hammingWindow(i) +- hammingPrecision)
    }
  }

  it should "apply Hamming window" in {
    val window = framer.scaledHammingWindow(10, 1D / Short.MaxValue)
    val input = (0 until 10).map(_ => Short.MaxValue)
    val output = framer.applyHammingWindow(window, input)

    output should have length input.length
    hammingWindow.indices.foreach { i =>
      output(i) should be (hammingWindow(i) +- hammingPrecision)
    }
  }
}
