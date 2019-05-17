package chromaprint

import fs2.Pipe

object silenceRemover {

  object Config {

    object Defaults {
      val threshold: Short = 0
      val window: Int = 55 // ~5 ms at 11025 Hz
    }

    val default: Config = Config(
      Defaults.threshold,
      Defaults.window
    )

    def apply(threshold: Short): Config =
      Config(threshold, Defaults.window)

  }

  final case class Config
  (
    threshold: Short,
    window: Int
  )

  def pipe[F[_]](config: silenceRemover.Config): Pipe[F,Short,Short] =
    _.mapAccumulate[Option[MovingAverage],Option[Short]](Some(MovingAverage(config.window))){
      case (opt, b) =>
        opt map (_.append(b.toLong.abs)) filter(_.average < config.threshold) match {
          case Some(ma) =>
            (Some(ma), None)
          case _ =>
            (None, Some(b))
        }
    }.map(_._2).unNone

}
