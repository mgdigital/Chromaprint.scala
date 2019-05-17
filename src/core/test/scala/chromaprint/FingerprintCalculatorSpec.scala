package chromaprint

import fs2.{Pure, Stream}

class FingerprintCalculatorSpec extends AbstractSpec {

  behavior of "Fingerprint calculator"

  val classifierConfig =
    Classifier.Config(
      Vector(
        Classifier(Classifier.Filter(0, 0, 1, 1), Classifier.Quantizer(0.01, 1.01, 1.5))
      )
    )

  it should "calculate a sub-fingerprint" in {
    val integral = Stream[Pure,Vector[Double]](
      Vector(0, 1),
      Vector(2, 3)
    ).through(IntegralImage.pipe).compile.toVector

    FingerprintCalculator.subFingerprint(classifierConfig, integral, 0) should equal (FingerprintCalculator.grayCode(0))
    FingerprintCalculator.subFingerprint(classifierConfig, integral, 1) should equal (FingerprintCalculator.grayCode(2))
  }

  it should "calculate a fingerprint" in {
    val rawFingerprint = Stream[Pure,Vector[Double]](
      Vector(0, 1),
      Vector(2, 3),
      Vector(4, 5)
    ).through(IntegralImage.pipe)
      .through(FingerprintCalculator.pipe(classifierConfig)).compile.toVector

    rawFingerprint should equal (Vector(0, 2, 3).map(FingerprintCalculator.grayCode))
  }
}
