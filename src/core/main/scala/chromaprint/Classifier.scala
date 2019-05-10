package chromaprint

import scala.math.log

object Classifier {

  def subtractLog(a: Double, b: Double): Double =
    log(a + 1) - log(b + 1)

  type Extract = (Image, Int, Int, Int, Int) => Double

  final case class FilterType
  (
    extractA: Extract,
    extractB: Extract
  )
  {

    def apply
    (
      image: Image,
      x: Int,
      y: Int,
      w: Int,
      h: Int
    ): Double = {
      require(x >= 0 && y >= 0 && w >= 1 && h >= 1)
      subtractLog(
        extractA(image, x, y, w, h),
        extractB(image, x, y, w, h)
      )
    }
  }

  val filter0: FilterType = FilterType(
    (i, x, y, w, h) =>
      i.area(x, y, x + w - 1, y + h - 1),
    (_, _, _, _, _) =>
      0D
  )

  val filter1: FilterType = FilterType(
    (i, x, y, w, h) =>
      i.area(x, y + (h / 2), x + w - 1, y + h - 1),
    (i, x, y, w, h) =>
      i.area(x, y, x + w - 1, y + (h / 2) - 1)
  )

  val filter2: FilterType = FilterType(
    (i, x, y, w, h) =>
      i.area(x + (w / 2), y, x + w - 1, y + h - 1),
    (i, x, y, w, h) =>
      i.area(x, y, x + (w / 2) - 1, y + h - 1)
  )

  val filter3: FilterType = FilterType(
    (i, x, y, w, h) =>
      i.area(x, y + (h / 2), x + (w / 2) - 1, y + h - 1) +
        i.area(x + (w / 2), y, x + w - 1, y + (h / 2) - 1),
    (i, x, y, w, h) =>
      i.area(x, y, x + (w / 2) - 1, y + (h / 2) - 1) +
        i.area(x + (w / 2), y + (h / 2), x + w - 1, y + h - 1)
  )

  val filter4: FilterType = FilterType(
    (i, x, y, w, h) =>
      i.area(x, y + (h / 3), x + w - 1, y + (2 * h / 3) - 1),
    (i, x, y, w, h) =>
      i.area(x, y, x + w - 1, y + (h / 3) - 1) +
        i.area(x, y + (2 * h / 3), x + w - 1, y + h - 1)
  )

  val filter5: FilterType = FilterType(
    (i, x, y, w, h) =>
      i.area(x + (w / 3), y, x + (2 * w / 3) - 1, y + h - 1),
    (i, x, y, w, h) =>
      i.area(x, y, x + (w / 3) - 1, y + h - 1) +
        i.area(x + (2 * w / 3), y, x + w - 1, y + h - 1)
  )

  val filters: Vector[FilterType] = Vector(
    filter0,
    filter1,
    filter2,
    filter3,
    filter4,
    filter5
  )

  object Filter {

    def apply
    (
      filterNum: Int,
      y: Int,
      height: Int,
      width: Int
    ): Filter =
      Filter(
        filters(filterNum),
        y,
        height,
        width
      )
  }

  final case class Filter
  (
    filterType: FilterType,
    y: Int,
    height: Int,
    width: Int
  ) {

    def apply(image: Image, x: Int): Double =
      filterType(image, x, y, width, height)
  }

  final case class Quantizer
  (
    t0: Double,
    t1: Double,
    t2: Double
  ) {

    def apply(value: Double): Int = {
      if (value < t1) {
        if (value < t0) {
          0
        } else {
          1
        }
      } else if (value < t2) {
        2
      } else {
        3
      }
    }
  }

  final case class Config(classifiers: Vector[Classifier]) {

    lazy val length: Int = classifiers.length

    lazy val maxFilterWidth: Int = classifiers.map(_.filter.width).foldLeft(0)(_ max _)
  }
}

final case class Classifier(filter: Classifier.Filter, quantizer: Classifier.Quantizer) {

  def apply(image: Image, offset: Int): Int =
    filter(image, offset) |>
      (quantizer(_))
}
