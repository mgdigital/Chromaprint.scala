package chromaprint

object silenceRemover {

  val silenceWindow: Int =
    55 // ~5 ms at 11025 Hz

  val newMovingAverage: MovingAverage =
    MovingAverage(silenceWindow)

  def apply(threshold: Short, input: Seq[Short]): Seq[Short] = {
    var ma: MovingAverage = newMovingAverage
    input.dropWhile { i =>
      ma = ma.append(i.abs)
      ma.average < threshold
    }
  }
}
