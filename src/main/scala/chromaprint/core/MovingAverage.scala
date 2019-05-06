package chromaprint.core

final case class MovingAverage (window: Int, data: List[Short] = List.empty) {

  lazy val length: Int = data.length

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
