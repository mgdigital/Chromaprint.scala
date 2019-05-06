package chromaprint.core

object Image {

  def apply(columns: Int): Image =
    new Image(columns, Vector.empty)

  def newImage: Image =
    apply(chroma.numBands)

  def apply(features: Seq[Vector[Double]]): Image =
    features.foldLeft(Image.newImage){
      (i, f) => i.addRow(f)
    }
}

final class Image private (
  val columns: Int,
  val data: Vector[Vector[Double]]
) {

  require(columns > 0)

  val rows: Int =
    data.length

  val size: Int =
    columns * rows

  val isEmpty: Boolean =
    size == 0

  val nonEmpty: Boolean =
    !isEmpty

  def validX(x: Int): Boolean =
    x >= 0 && x < columns

  def validY(y: Int): Boolean =
    y >= 0 && y < rows

  def validCoords(x: Int, y: Int): Boolean =
    validX(x) && validY(y)

  def validIndex(i: Int): Boolean =
    i >= 0 && i < size

  def apply(y: Int, x: Int): Double = {
    require(validCoords(x, y))
    data(y)(x)
  }

  def coords(i: Int): (Int, Int) = {
    require(validIndex(i))
    ((i / columns).floor.toInt, i % columns)
  }

  def apply(i: Int): Double = {
    require(validIndex(i))
    val coords = this.coords(i)
    apply(coords._1, coords._2)
  }

  def set(y: Int, x: Int, value: Double): Image = {
    require(validCoords(x, y))
    new Image(columns, data.updated(y, data(y).updated(x, value)))
  }

  def setIndex(i: Int, value: Double): Image =
    coords(i) |>
      (c => set(c._1, c._2, value))

  def row(y: Int): Vector[Double] = {
    require(validY(y))
    data(y)
  }

  def addRow(row: Vector[Double]): Image = {
    require(row.length == columns)
    new Image(columns, data :+ row)
  }

  def area(x1: Int, y1: Int, x2: Int, y2: Int): Double =
    if (x2 < x1 || y2 < y1) {
      0D
    } else {
      var area: Double = apply(x2, y2)
      if (x1 > 0) {
        area -= apply(x1 - 1, y2)
        if (y1 > 0) {
          area += apply(x1 - 1, y1 - 1)
        }
      }
      if (y1 > 0) {
        area -= apply(x2, y1 - 1)
      }
      area
    }

  def integrate: Image = {
    var image: Image = this
    var current: Int = 1
    var last: Int = 0
    if (nonEmpty) {
      (0 until columns - 1) foreach { _ =>
        image = image.setIndex(current, image(current) + image(current - 1))
        current += 1
      }
    }
    (0 until rows - 1) foreach { _ =>
      image = image.setIndex(current, image(current) + image(last))
      current += 1
      last += 1
      (0 until columns - 1) foreach { _ =>
        image = image.setIndex(current, image(current) + image(current - 1) + image(last) - image(last - 1))
        current += 1
        last += 1
      }
    }
    image
  }

}
