package chromaprint.core

class ClassifierSpec extends AbstractSpec {

  behavior of "Classifier"

  import Classifier.{Filter, Quantizer}

  it should "filter" in {
    val image = Image(2)
      .addRow(Vector(0, 1))
      .addRow(Vector(2, 3))
    val filter = Filter(0, 0, 1, 1)
    val integral = image.integrate

    filter(integral, 0) should equal (0)
    filter(integral, 1) should be (1.0986123 +- 0.0001)
  }

  it should "quantize" in {
    val q = Quantizer(0, 0.1, 0.3)

    q(-0.1) should equal (0)
    q(0) should equal (1)
    q(0.03) should equal (1)
    q(0.1) should equal (2)
    q(0.13) should equal (2)
    q(0.3) should equal (3)
    q(0.33) should equal (3)
    q(1000) should equal (3)
  }
}
