package chromaprint

import fs2.{Pure,Stream}

class silenceRemoverSpec extends AbstractSpec {

  behavior of "Silence remover"

  it should "passthrough" in {
    val samples: Vector[Short] = Vector(0, 1000, 2000, 3000, 4000, 5000, 6000)
    val processed = Stream(samples:_*).through(silenceRemover.pipe(silenceRemover.Config(0)))
        .compile.toVector

    processed should equal (samples)
  }

  it should "remove leading silence" in {
    val samples: Vector[Short] = Vector(0, 60, 0, 1000, 2000, 0, 4000, 5000, 0)
    val processed = Stream(samples:_*).through(silenceRemover.pipe(silenceRemover.Config(100)))
      .compile.toVector

    processed should equal (samples.drop(3))
  }
}
