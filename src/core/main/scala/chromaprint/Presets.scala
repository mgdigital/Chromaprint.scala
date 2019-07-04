package chromaprint

object Presets {

  // scalastyle:off magic.number

  lazy val algorithm1: Config =
    Config.default.copy(
      algorithm = 1,
      classifiers = ClassifierPresets(0)
    )

  lazy val algorithm2: Config =
    Config.default

  lazy val algorithm3: Config =
    Config.default.copy(
      algorithm = 3,
      classifiers = ClassifierPresets(2),
      interpolate = true
    )

  lazy val algorithm4: Config =
    Config.default.copy(
      algorithm = 4,
      silenceRemover = SilenceRemover.Config.default.copy(threshold = 50)
    )

  def apply(algorithm: Int): Option[Config] =
    algorithm match {
      case 1 =>
        Some(algorithm1)
      case 2 =>
        Some(algorithm2)
      case 3 =>
        Some(algorithm3)
      case 4 =>
        Some(algorithm4)
      case _ =>
        None
    }

  // scalastyle:on magic.number

  lazy val default: Config =
    algorithm2

  def exists(algorithm: Int): Boolean =
    apply(algorithm).isDefined
}
