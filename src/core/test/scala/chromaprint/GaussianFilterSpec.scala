package chromaprint

import org.scalactic.TripleEqualsSupport.Spread

class GaussianFilterSpec extends AbstractSpec {

  behavior of "Gaussian filter"

  it should "iterate" in {
    val data: Vector[Int] = (1 to 9).toVector
    var it: GaussianFilter.ReflectIterator = GaussianFilter.ReflectIterator(data.length)
    (0 until 3) foreach { _ =>
      it = it.moveBack
    }
    data(it.pos) should equal (3)
    it.forwardDistance should equal (0)
    it = it.moveForward
    data(it.pos) should equal (2)
    it.forwardDistance should equal (0)
    it = it.moveForward
    data(it.pos) should equal (1)
    it.forwardDistance should equal (0)
    it = it.moveForward
    data(it.pos) should equal (1)
    it.forwardDistance should equal (8)
    it = it.moveForward
    data(it.pos) should equal (2)
  }

  val input: Vector[Float] = Vector(1F, 2F, 4F)

  it should "box filter width 1" in {
    GaussianFilter.boxFilter(1, input) should equal (Vector(1F, 2F, 4F))
  }

  it should "box filter width 2" in {
    GaussianFilter.boxFilter(2, input) should equal (Vector(1F, 1.5F, 3F))
  }

  it should "box filter width 3" in {
    val third = 1F / 3
    GaussianFilter.boxFilter(3, input) should equal (Vector(1F + third, 2F + third, 3F + third))
  }

  it should "box filter width 4" in {
    GaussianFilter.boxFilter(4, input) should equal (Vector(1.5F, 2F, 2.75F))
  }

  it should "box filter width 5" in {
    GaussianFilter.boxFilter(5, input) should equal (Vector(2F, 2.4F, 2.6F))
  }

  def roughly(v: Float): Spread[Float] =
    v +- 0.0000001F

  it should "gaussian filter 1" in {
    val filtered = GaussianFilter(1.6, 3, input)
    filtered should have length 3
    filtered(0) should be (roughly(1.88888889F))
    filtered(1) should be (roughly(2.33333333F))
    filtered(2) should be (roughly(2.77777778F))
  }

  it should "gaussian filter 2" in {
    val filtered = GaussianFilter(3.6, 4, input)
    filtered should have length 3
    filtered(0) should be (roughly(2.3322449F))
    filtered(1) should be (roughly(2.33306122F))
    filtered(2) should be (roughly(2.33469388F))
  }

}
