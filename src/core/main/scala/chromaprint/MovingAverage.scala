package chromaprint

object MovingAverage {

  def apply(window: Int): MovingAverage =
    new MovingAverage(window, List.empty)

  def calculate(size: Int, sum: Long): Long =
    if (size == 0) {
      0
    } else {
      sum / size
    }

}

final case class MovingAverage (window: Int, data: List[Long]) {

  require(window >= 1)

  val size: Int = data.length

  require(size <= window)

  lazy val sum: Long = data.sum

  lazy val value: Long = MovingAverage.calculate(size, sum)

  def append(value: Long): MovingAverage =
    copy(data = (value :: data) take window)
}
