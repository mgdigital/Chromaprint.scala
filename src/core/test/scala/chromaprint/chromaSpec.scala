package chromaprint

class chromaSpec extends AbstractSpec {

  behavior of "chroma"

  val range: chroma.Range =
    chroma.Range(
      10,
      510,
      256,
      1000
    )

  it should "normal A" in {
    val frame = Vector.fill[Double](128)(0D)
      .updated(113, 1D)
    val features: Vector[Double] =
      chroma(chroma.Config(range), Seq(frame)).flatten.toVector

    features should equal (Vector(
      1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
      0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    ))
  }

  it should "normal G sharp" in {
    val frame = Vector.fill[Double](128)(0D)
      .updated(112, 1D)
    val features: Vector[Double] =
      chroma(chroma.Config(range), Seq(frame)).flatten.toVector

    features should equal (Vector(
      0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
      0.0, 0.0, 0.0, 0.0, 0.0, 1.0
    ))
  }

  it should "normal B" in {
    val frame = Vector.fill[Double](128)(0D)
      .updated(64, 1D) // 250 Hz
    val features: Vector[Double] =
      chroma(chroma.Config(range), Seq(frame)).flatten.toVector

    features should equal (Vector(
      0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
      0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    ))
  }

  def assertFeaturesTolerance(actual: Vector[Double], expected: Vector[Double]): Unit = {
    actual should have length expected.length
    expected.indices.foreach { i =>
      actual(i) should be (expected(i) +- 0.0001)
    }
  }

  it should "interpolated B" in {
    val frame =
      Vector.fill[Double](128)(0D)
        .updated(64, 1D)
    val expectedFeatures: Vector[Double] =
      Vector(
        0.0, 0.286905, 0.713095, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0
      )
    val features: Vector[Double] =
      chroma(chroma.Config(range, interpolate = true), Seq(frame)).flatten.toVector

    assertFeaturesTolerance(features, expectedFeatures)
  }

  it should "interpolated A" in {
    val frame =
      Vector.fill[Double](128)(0D)
        .updated(113, 1D)
    val expectedFeatures: Vector[Double] =
      Vector(
        0.555242, 0, 0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.444758
      )
    val features: Vector[Double] =
      chroma(chroma.Config(range, interpolate = true), Seq(frame)).flatten.toVector

    assertFeaturesTolerance(features, expectedFeatures)
  }

  it should "interpolated G sharp" in {
    val frame =
      Vector.fill[Double](128)(0D)
        .updated(112, 1D)
    val expectedFeatures: Vector[Double] =
      Vector(
        0.401354, 0, 0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.598646
      )
    val features: Vector[Double] =
      chroma(chroma.Config(range, interpolate = true), Seq(frame)).flatten.toVector

    assertFeaturesTolerance(features, expectedFeatures)
  }
}
