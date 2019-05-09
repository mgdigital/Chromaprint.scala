package chromaprint

class fingerprintCalculatorSpec extends AbstractSpec {

  behavior of "Fingerprint calculator"

  val classifierConfig =
    Classifier.Config(
      Vector(
        Classifier(Classifier.Filter(0, 0, 1, 1), Classifier.Quantizer(0.01, 1.01, 1.5))
      )
    )

  it should "calculate a sub-fingerprint" in {
    val image = Image(2)
      .addRow(Vector(0, 1))
      .addRow(Vector(2, 3))
    val integral = image.integrate

    fingerprintCalculator.subFingerprint(classifierConfig, integral, 0) should equal (fingerprintCalculator.grayCode(0))
    fingerprintCalculator.subFingerprint(classifierConfig, integral, 1) should equal (fingerprintCalculator.grayCode(2))
  }

  it should "calculate a fingerprint" in {
    val image = Image(2)
      .addRow(Vector(0, 1))
      .addRow(Vector(2, 3))
      .addRow(Vector(4, 5))

      fingerprintCalculator(classifierConfig, image) should equal (Vector(0, 2, 3).map(fingerprintCalculator.grayCode))
  }
}
