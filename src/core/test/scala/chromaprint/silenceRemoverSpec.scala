package chromaprint

class silenceRemoverSpec extends AbstractSpec {

  behavior of "Silence remover"

  it should "passthrough" in {
    val samples: Seq[Short] = Seq(0, 1000, 2000, 3000, 4000, 5000, 6000)
    val processed = silenceRemover(0, samples)

    processed should equal (samples)
  }

  it should "remove leading silence" in {
    val samples: Seq[Short] = Seq(0, 60, 0, 1000, 2000, 0, 4000, 5000, 0)
    val processed = silenceRemover(100, samples)

    processed should equal (samples.drop(3))
  }
}
