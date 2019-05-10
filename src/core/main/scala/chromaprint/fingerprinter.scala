package chromaprint

object fingerprinter extends fingerprinter

trait fingerprinter {

  def apply
  (
    strAudioSource: String
  )(implicit fftImpl: FFT): Either[AudioSource.AudioSourceException,Fingerprint] =
    AudioSource(strAudioSource) match {
      case Left(e) =>
        Left(e)
      case Right(audioSource) =>
        apply(Presets.default, audioSource)
    }

  def apply
  (
    audioSource: AudioSource
  )(implicit fftImpl: FFT): Either[AudioSource.AudioSourceException,Fingerprint] =
    apply(Presets.default, audioSource)

  def apply
  (
    config: Config,
    audioSource: AudioSource
  )(implicit fftImpl: FFT): Either[AudioSource.AudioSourceException,Fingerprint] =
    audioSource.dataStream(config.sampleRate) match {
      case Left(e) =>
        Left(e)
      case Right(audio) =>
        Right(
          apply(
            config,
            audio
          )
        )
    }

  def apply
  (
    audio: Seq[Short]
  )(implicit fftImpl: FFT): Fingerprint =
    apply(Presets.default, audio)

  def apply
  (
    config: Config,
    audio: Seq[Short]
  )(implicit fftImpl: FFT): Fingerprint = {

    def truncated: Seq[Short] =
      audio.take(config.maxBytes)

    def removeSilence(audio: Seq[Short]): Seq[Short] =
      silenceRemover(config.silenceThreshold, audio)

    def extractFrames(audio: Seq[Short]): Seq[Vector[Double]] =
      framer(config.framerConfig, audio)

    def extractFeatures(frames: Seq[Vector[Double]]): Seq[Vector[Double]] =
      chroma(config.chromaConfig, frames) |>
        chromaFilter.apply |>
        chromaNormalizer.apply

    def createImage(featureFrames: Seq[Vector[Double]]): Image =
      Image(featureFrames).integrate

    def createFingerprint(integral: Image): Fingerprint =
      Fingerprint(
        config.algorithm,
        if (config.captureDuration) {
          Some(audio.length.toFloat / config.sampleRate)
        } else {
          None
        },
        fingerprintCalculator(
          config.classifiers,
          integral
        )
      )

    truncated |>
      removeSilence |>
      extractFrames |>
      fftImpl.apply |>
      extractFeatures |>
      createImage |>
      createFingerprint
  }
}
