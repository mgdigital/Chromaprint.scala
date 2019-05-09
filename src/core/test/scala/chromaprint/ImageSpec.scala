package chromaprint

class ImageSpec extends AbstractSpec {

  behavior of "Image"

  it should "create an image" in {
    val image = Image(2)
      .addRow(Vector(0, 1))
      .addRow(Vector(2, 3))
      .addRow(Vector(4, 5))

    image(0, 0) should equal (0D)
    image(0, 1) should equal (1D)
    image(2, 0) should equal (4D)
    image.coords(0) should equal ((0, 0))
    image.coords(1) should equal ((0, 1))
    image.coords(2) should equal ((1, 0))
    image.coords(3) should equal ((1, 1))
    image(0) should equal (0D)
    image(2) should equal (2D)
    image(3) should equal (3D)
  }

  it should "integrate basic 2d" in {
    val image = Image(2)
      .addRow(Vector(1.0, 2.0))
      .addRow(Vector(3.0, 4.0))
    val integral = image.integrate

    integral(0, 0) should equal (1D)
    integral(0, 1) should equal (3D)
    integral(1, 0) should equal (4D)
    integral(1, 1) should equal (10D)
  }

  it should "integrate vertical 1D" in {
    val image = Image(1)
      .addRow(Vector(1))
      .addRow(Vector(2))
      .addRow(Vector(3))
    val integral = image.integrate

    integral(0, 0) should equal (1D)
    integral(1, 0) should equal (3D)
    integral(2, 0) should equal (6D)
  }

  it should "integrate horizontal 1D" in {
    val image = Image(3)
      .addRow(Vector(1, 2, 3))
    val integral = image.integrate

    integral(0, 0) should equal (1D)
    integral(0, 1) should equal (3D)
    integral(0, 2) should equal (6D)
  }

  it should "integrate 3x3" in {
    val image = Image(3)
      .addRow(Vector(1, 2, 3))
      .addRow(Vector(4, 5, 6))
      .addRow(Vector(7, 8, 9))
    val integral = image.integrate

    integral.row(0) should equal (Vector(1, 3, 6))
    integral.row(1) should equal (Vector(5, 12, 21))
    integral.row(2) should equal (Vector(12, 27, 45))
  }

  it should "calculate the area" in {
    val image = Image(3)
      .addRow(Vector(1, 2, 3))
      .addRow(Vector(4, 5, 6))
      .addRow(Vector(7, 8, 9))
    val integral = image.integrate

    integral.area(0, 0, 0, 0) should equal (1)
    integral.area(0, 0, 1, 0) should equal (1 + 4)
    integral.area(0, 0, 2, 0) should equal (1 + 4 + 7)
    integral.area(0, 0, 0, 1) should equal (1 + 2)
    integral.area(0, 0, 1, 1) should equal (1 + 4 + 2 + 5)
    integral.area(0, 0, 2, 1) should equal (1 + 4 + 7 + 2 + 5 + 8)
    integral.area(0, 1, 0, 1) should equal (2)
    integral.area(0, 1, 1, 1) should equal (2 + 5)
    integral.area(0, 1, 2, 1) should equal (2 + 5 + 8)
  }

}
