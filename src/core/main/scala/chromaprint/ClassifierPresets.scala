package chromaprint

import Classifier.{Filter,Quantizer}

object ClassifierPresets {

  // scalastyle:off magic.number

  private object Factory {

    // Used for http://oxygene.sk/lukas/2010/07/introducing-chromaprint/
    // Trained on a randomly selected test data
    def preset0: Classifier.Config =
      Classifier.Config(
        Vector(
          Classifier(Filter(0, 0, 3, 15), Quantizer(2.10543, 2.45354, 2.69414)),
          Classifier(Filter(1, 0, 4, 14), Quantizer(-0.345922, 0.0463746, 0.446251)),
          Classifier(Filter(1, 4, 4, 11), Quantizer(-0.392132, 0.0291077, 0.443391)),
          Classifier(Filter(3, 0, 4, 14), Quantizer(-0.192851, 0.00583535, 0.204053)),
          Classifier(Filter(2, 8, 2, 4), Quantizer(-0.0771619, -0.00991999, 0.0575406)),
          Classifier(Filter(5, 6, 2, 15), Quantizer(-0.710437, -0.518954, -0.330402)),
          Classifier(Filter(1, 9, 2, 16), Quantizer(-0.353724, -0.0189719, 0.289768)),
          Classifier(Filter(3, 4, 2, 10), Quantizer(-0.128418, -0.0285697, 0.0591791)),
          Classifier(Filter(3, 9, 2, 16), Quantizer(-0.139052, -0.0228468, 0.0879723)),
          Classifier(Filter(2, 1, 3, 6), Quantizer(-0.133562, 0.00669205, 0.155012)),
          Classifier(Filter(3, 3, 6, 2), Quantizer(-0.0267, 0.00804829, 0.0459773)),
          Classifier(Filter(2, 8, 1, 10), Quantizer(-0.0972417, 0.0152227, 0.129003)),
          Classifier(Filter(3, 4, 4, 14), Quantizer(-0.141434, 0.00374515, 0.149935)),
          Classifier(Filter(5, 4, 2, 15), Quantizer(-0.64035, -0.466999, -0.285493)),
          Classifier(Filter(5, 9, 2, 3), Quantizer(-0.322792, -0.254258, -0.174278)),
          Classifier(Filter(2, 1, 8, 4), Quantizer(-0.0741375, -0.00590933, 0.0600357))
        )
      )

    // Trained on 60k pairs based on eMusic samples (mp3)
    def preset1: Classifier.Config =
      Classifier.Config(
        Vector(
          Classifier(Filter(0, 4, 3, 15), Quantizer(1.98215, 2.35817, 2.63523)),
          Classifier(Filter(4, 4, 6, 15), Quantizer(-1.03809, -0.651211, -0.282167)),
          Classifier(Filter(1, 0, 4, 16), Quantizer(-0.298702, 0.119262, 0.558497)),
          Classifier(Filter(3, 8, 2, 12), Quantizer(-0.105439, 0.0153946, 0.135898)),
          Classifier(Filter(3, 4, 4, 8), Quantizer(-0.142891, 0.0258736, 0.200632)),
          Classifier(Filter(4, 0, 3, 5), Quantizer(-0.826319, -0.590612, -0.368214)),
          Classifier(Filter(1, 2, 2, 9), Quantizer(-0.557409, -0.233035, 0.0534525)),
          Classifier(Filter(2, 7, 3, 4), Quantizer(-0.0646826, 0.00620476, 0.0784847)),
          Classifier(Filter(2, 6, 2, 16), Quantizer(-0.192387, -0.029699, 0.215855)),
          Classifier(Filter(2, 1, 3, 2), Quantizer(-0.0397818, -0.00568076, 0.0292026)),
          Classifier(Filter(5, 10, 1, 15), Quantizer(-0.53823, -0.369934, -0.190235)),
          Classifier(Filter(3, 6, 2, 10), Quantizer(-0.124877, 0.0296483, 0.139239)),
          Classifier(Filter(2, 1, 1, 14), Quantizer(-0.101475, 0.0225617, 0.231971)),
          Classifier(Filter(3, 5, 6, 4), Quantizer(-0.0799915, -0.00729616, 0.063262)),
          Classifier(Filter(1, 9, 2, 12), Quantizer(-0.272556, 0.019424, 0.302559)),
          Classifier(Filter(3, 4, 2, 14), Quantizer(-0.164292, -0.0321188, 0.0846339))
        )
      )

    // Trained on 60k pairs based on eMusic samples with interpolation enabled (mp3)
    def preset2: Classifier.Config =
      Classifier.Config(
        Vector(
          Classifier(Filter(0, 4, 3, 15), Quantizer(1.98215, 2.35817, 2.63523)),
          Classifier(Filter(4, 4, 6, 15), Quantizer(-1.03809, -0.651211, -0.282167)),
          Classifier(Filter(1, 0, 4, 16), Quantizer(-0.298702, 0.119262, 0.558497)),
          Classifier(Filter(3, 8, 2, 12), Quantizer(-0.105439, 0.0153946, 0.135898)),
          Classifier(Filter(3, 4, 4, 8), Quantizer(-0.142891, 0.0258736, 0.200632)),
          Classifier(Filter(4, 0, 3, 5), Quantizer(-0.826319, -0.590612, -0.368214)),
          Classifier(Filter(1, 2, 2, 9), Quantizer(-0.557409, -0.233035, 0.0534525)),
          Classifier(Filter(2, 7, 3, 4), Quantizer(-0.0646826, 0.00620476, 0.0784847)),
          Classifier(Filter(2, 6, 2, 16), Quantizer(-0.192387, -0.029699, 0.215855)),
          Classifier(Filter(2, 1, 3, 2), Quantizer(-0.0397818, -0.00568076, 0.0292026)),
          Classifier(Filter(5, 10, 1, 15), Quantizer(-0.53823, -0.369934, -0.190235)),
          Classifier(Filter(3, 6, 2, 10), Quantizer(-0.124877, 0.0296483, 0.139239)),
          Classifier(Filter(2, 1, 1, 14), Quantizer(-0.101475, 0.0225617, 0.231971)),
          Classifier(Filter(3, 5, 6, 4), Quantizer(-0.0799915, -0.00729616, 0.063262)),
          Classifier(Filter(1, 9, 2, 12), Quantizer(-0.272556, 0.019424, 0.302559)),
          Classifier(Filter(3, 4, 2, 14), Quantizer(-0.164292, -0.0321188, 0.0846339))
        )
      )

  }

  lazy val preset0: Classifier.Config =
    Factory.preset0

  lazy val preset1: Classifier.Config =
    Factory.preset1

  lazy val preset2: Classifier.Config =
    Factory.preset2

  // scalastyle:on magic.number

  lazy val presets: Vector[Classifier.Config] =
    Vector(
      preset0,
      preset1,
      preset2
    )

  def apply(i: Int): Classifier.Config =
    presets(i)

  lazy val default: Classifier.Config =
    preset1
}
