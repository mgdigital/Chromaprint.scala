package chromaprint

import fs2.Pipe

object Framer {

  final case class Config
  (
    frameSize: Int,
    overlap: Int
  )

  def pipe[F[_]](config: Config): Pipe[F,Short,IndexedSeq[Short]] =
    _.mapAccumulate[Vector[Short],Option[Vector[Short]]](Vector.empty[Short]){
      (vec, b) =>
        vec :+ b match {
          case vec2 if vec2.length == config.frameSize =>
            (vec2 takeRight config.overlap, Some(vec2))
          case vec2 =>
            (vec2, None)
        }
    }.map(_._2).unNone

}
