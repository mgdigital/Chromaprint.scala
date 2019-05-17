package chromaprint

object MovingAverage {

  def apply(window: Int): MovingAverage =
    new MovingAverage(window, List.empty)
}

final case class MovingAverage (window: Int, data: List[Long]) {

  require(window >= 1)

  val length: Int = data.length

  require(length <= window)

  lazy val sum: Long = data.sum

  lazy val average: Long =
    if (length == 0) {
      0
    } else {
      sum / length
    }

  def append(value: Long): MovingAverage =
    copy(data = (value :: data) take window)
}
