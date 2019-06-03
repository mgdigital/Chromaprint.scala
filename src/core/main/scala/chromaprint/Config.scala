package chromaprint

object Config {

  object Defaults {
    val algorithm: Int = 2
    val sampleRate: Int = 11025
    val frameSize: Int = 4096
    val overlap: Int = (frameSize.toDouble * 2 / 3).ceil.toInt
    val minFreq: Int = 28
    val maxFreq: Int = 3520
    val maxDuration: Int = 120
    val removeSilence: SilenceRemover.Config = SilenceRemover.Config.default
    val classifiers: Classifier.Config = ClassifierPresets.default
    val interpolate: Boolean = false
  }

  val default = Config(
    Defaults.algorithm,
    Defaults.sampleRate,
    Defaults.frameSize,
    Defaults.overlap,
    Defaults.minFreq,
    Defaults.maxFreq,
    Defaults.maxDuration,
    Defaults.removeSilence,
    Defaults.classifiers,
    Defaults.interpolate
  )
}

final case class Config
(
  algorithm: Int,
  sampleRate: Int,
  frameSize: Int,
  overlap: Int,
  minFreq: Int,
  maxFreq: Int,
  maxDuration: Int,
  silenceRemover: chromaprint.SilenceRemover.Config,
  classifiers: Classifier.Config,
  interpolate: Boolean
) {

  lazy val maxLength: Int =
    maxDuration * sampleRate

  lazy val framerConfig: Framer.Config =
    Framer.Config(
      frameSize,
      overlap
    )

  lazy val chromaRange: Chroma.Range =
    Chroma.Range(
      minFreq,
      maxFreq,
      frameSize,
      sampleRate
    )

  lazy val chromaConfig: Chroma.Config =
    Chroma.Config(
      chromaRange,
      interpolate
    )

  lazy val hammingWindow: IndexedSeq[Double] =
    HammingWindow.short(frameSize)
}
