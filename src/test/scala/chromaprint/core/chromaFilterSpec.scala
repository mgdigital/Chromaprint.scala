package chromaprint.core

class chromaFilterSpec extends AbstractSpec {

  behavior of "Chroma filter"

  it should "blur 2" in {
    val coefficients = Vector(0.5, 0.5)
    val chunks: Seq[Vector[Double]] = Seq(
      Vector( 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ),
      Vector( 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ),
      Vector( 2, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 )
    )

    val image = Image(chromaFilter(coefficients, chunks))

    image(0, 0) should equal (0.5)
    image(1, 0) should equal (1.5)
    image(0, 1) should equal (5.5)
    image(1, 1) should equal (6.5)
  }

  it should "blur 3" in {
    val coefficients = Vector(0.5, 0.7, 0.5)
    val chunks: Seq[Vector[Double]] = Seq(
      Vector( 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ),
      Vector( 1.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ),
      Vector( 2.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ),
      Vector( 3.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 )
    )

    val image = Image(chromaFilter(coefficients, chunks))

    image.rows should equal (2)
    image(0, 0) should equal (1.7)
    image(1, 0) should equal (3.4)
    image(0, 1) should equal (10.2)
    image(1, 1) should be (11.9 +- 0.00001)
  }

  it should "diff" in {
    val coefficients = Vector(1D, -1D)
    val chunks: Seq[Vector[Double]] = Seq(
      Vector( 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ),
      Vector( 1.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ),
      Vector( 2.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 )
    )

    val image = Image(chromaFilter(coefficients, chunks))

    image.rows should equal (2)
    image(0, 0) should equal (-1)
    image(1, 0) should equal (-1)
    image(0, 1) should equal (-1)
    image(1, 1) should equal (-1)
  }
}
