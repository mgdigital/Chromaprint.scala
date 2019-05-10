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
    val silenceThreshold: Short = 0
    val classifiers: Classifier.Config = ClassifierPresets.default
    val interpolate: Boolean = false
    val captureDuration: Boolean = true
  }

  val default = Config(
    Defaults.algorithm,
    Defaults.sampleRate,
    Defaults.frameSize,
    Defaults.overlap,
    Defaults.minFreq,
    Defaults.maxFreq,
    Defaults.maxDuration,
    Defaults.silenceThreshold,
    Defaults.classifiers,
    Defaults.interpolate,
    Defaults.captureDuration
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
  silenceThreshold: Short,
  classifiers: Classifier.Config,
  interpolate: Boolean,
  captureDuration: Boolean // Seq will be converted to IndexedSeq to capture length
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
