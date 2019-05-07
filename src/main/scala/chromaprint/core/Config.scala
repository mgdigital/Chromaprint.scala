package chromaprint.core

object Config {

  object Defaults {
    val algorithm: Int = 0
    val sampleRate: Int = 11025
    val frameSize: Int = 4096
    val overlap: Int = (frameSize.toDouble * 2 / 3).ceil.toInt
    val minFreq: Int = 28
    val maxFreq: Int = 3520
    val maxDuration: Int = 120
    val silenceThreshold: Short = 0
    val classifiers: Classifier.Config = ClassifierPresets.default
    val interpolate: Boolean = false
    val captureDuration: Boolean = true
  }
}

final case class Config
(
  algorithm: Int = Config.Defaults.algorithm,
  sampleRate: Int = Config.Defaults.sampleRate,
  frameSize: Int = Config.Defaults.frameSize,
  overlap: Int = Config.Defaults.overlap,
  minFreq: Int = Config.Defaults.minFreq,
  maxFreq: Int = Config.Defaults.maxFreq,
  maxDuration: Int = Config.Defaults.maxDuration,
  silenceThreshold: Short = Config.Defaults.silenceThreshold,
  classifiers: Classifier.Config = Config.Defaults.classifiers,
  interpolate: Boolean = Config.Defaults.interpolate,
  captureDuration: Boolean = Config.Defaults.captureDuration // Seq will be converted to IndexedSeq to capture length
) {

  lazy val maxBytes: Int =
    maxDuration * sampleRate

  lazy val framerConfig =
    framer.Config(
      frameSize,
      overlap
    )

  lazy val chromaRange =
    chroma.Range(
      minFreq,
      maxFreq,
      frameSize,
      sampleRate
    )

  lazy val chromaConfig =
    chroma.Config(
      chromaRange,
      interpolate
    )
}
