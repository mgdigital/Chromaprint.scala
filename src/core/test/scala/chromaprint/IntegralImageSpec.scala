package chromaprint

import fs2.{Pure, Stream}

class IntegralImageSpec extends AbstractSpec {

  behavior of "Integral image"

  def createIntegral(rows: Vector[Double]*): Vector[Vector[Double]] =
    Stream[Pure,Vector[Double]](rows :_*)
      .through(IntegralImage.pipe)
      .compile.toVector

  def area(integral: Vector[Vector[Double]], x1: Int, y1: Int, x2: Int, y2: Int): Double =
    IntegralImage.area(integral, x1, y1, x2, y2)

  it should "integrate basic 2d" in {
    val integral= createIntegral(
      Vector(1.0, 2.0),
      Vector(3.0, 4.0)
    )

    integral(0)(0) should equal (1D)
    integral(0)(1) should equal (3D)
    integral(1)(0) should equal (4D)
    integral(1)(1) should equal (10D)
  }

  it should "integrate vertical 1D" in {
    val integral = createIntegral(
      Vector(1),
      Vector(2),
      Vector(3)
    )

    integral(0)(0) should equal (1D)
    integral(1)(0) should equal (3D)
    integral(2)(0) should equal (6D)
  }

  it should "integrate horizontal 1D" in {
    val integral= createIntegral(Vector(1, 2, 3))

    integral(0)(0) should equal (1D)
    integral(0)(1) should equal (3D)
    integral(0)(2) should equal (6D)
  }

  it should "integrate 3x3" in {
    val integral = createIntegral(
      Vector(1, 2, 3),
      Vector(4, 5, 6),
      Vector(7, 8, 9)
    )

    integral(0) should equal (Vector(1, 3, 6))
    integral(1) should equal (Vector(5, 12, 21))
    integral(2) should equal (Vector(12, 27, 45))
  }

  it should "calculate the area" in {
    val integral = createIntegral(
      Vector(1, 2, 3),
      Vector(4, 5, 6),
      Vector(7, 8, 9)
    )

    area(integral, 0, 0, 0, 0) should equal (1)
    area(integral, 0, 0, 1, 0) should equal (1 + 4)
    area(integral, 0, 0, 2, 0) should equal (1 + 4 + 7)
    area(integral, 0, 0, 0, 1) should equal (1 + 2)
    area(integral, 0, 0, 1, 1) should equal (1 + 4 + 2 + 5)
    area(integral, 0, 0, 2, 1) should equal (1 + 4 + 7 + 2 + 5 + 8)
    area(integral, 0, 1, 0, 1) should equal (2)
    area(integral, 0, 1, 1, 1) should equal (2 + 5)
    area(integral, 0, 1, 2, 1) should equal (2 + 5 + 8)
  }

}
