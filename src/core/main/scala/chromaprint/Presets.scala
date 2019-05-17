package chromaprint

object Presets {

  // scalastyle:off magic.number

  val algorithm1: Config =
    Config.default.copy(
      algorithm = 1,
      classifiers = ClassifierPresets(0)
    )

  val algorithm2: Config =
    Config.default

  val algorithm3: Config =
    Config.default.copy(
      algorithm = 3,
      classifiers = ClassifierPresets(2),
      interpolate = true
    )

  val algorithm4: Config =
    Config.default.copy(
      algorithm = 4,
      silenceRemover = SilenceRemover.Config.default.copy(threshold = 50)
    )

  // scalastyle:on magic.number

  val map: Map[Int,Config] =
    Seq(
      algorithm1,
      algorithm2,
      algorithm3,
      algorithm4
    ).foldLeft(Map.empty[Int,Config]) {
      (m, p) => m.updated(p.algorithm, p)
    }

  def apply(algorithm: Int): Config =
    map(algorithm)

  val default: Config =
    algorithm2

  def exists(algorithm: Int): Boolean =
    map.contains(algorithm)
}
