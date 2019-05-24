package chromaprint

class GradientSpec extends AbstractSpec {

  behavior of "Gradient"

  it should "create an empty gradient" in {
    val gradient = Gradient(Vector.empty)
    gradient should have length 0
  }

  it should "create a 1 element gradient" in {
    val gradient = Gradient(Vector(1F))
    gradient should equal (Vector(0F))
  }

  it should "create a 2 element gradient" in {
    val gradient = Gradient(Vector(1F, 2F))
    gradient should equal (Vector(1F, 1F))
  }

  it should "create a 3 element gradient" in {
    val gradient = Gradient(Vector(1F, 2F, 4F))
    gradient should equal (Vector(1F, 1.5F, 2F))
  }

  it should "create a 4 element gradient" in {
    val gradient = Gradient(Vector(1F, 2F, 4F, 10F))
    gradient should equal (Vector(1F, 1.5F, 4F, 6F))
  }

}
