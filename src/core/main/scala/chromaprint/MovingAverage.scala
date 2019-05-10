package chromaprint

object MovingAverage {

  def apply(window: Int): MovingAverage =
    new MovingAverage(window, List.empty)
}

final case class MovingAverage (window: Int, data: List[Short]) {

  require(window >= 1)

  val length: Int = data.length

  require(length <= window)

  lazy val sum: Int = data.sum

  lazy val average: Int =
    if (length == 0) {
      0
    } else {
      sum / length
    }

  def append(value: Short): MovingAverage =
    copy(data = (value :: data) take window)
}
