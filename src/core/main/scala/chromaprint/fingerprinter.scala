package chromaprint

object fingerprinter extends fingerprinter

trait fingerprinter {

  def apply
  (
    strAudioSource: String
  )(implicit fftProvider: FFT): Either[AudioSource.AudioSourceException,Fingerprint] =
    AudioSource(strAudioSource) match {
      case Left(e) =>
        Left(e)
      case Right(audioSource) =>
        apply(Presets.default, audioSource)
    }

  def apply
  (
    audioSource: AudioSource
  )(implicit fftProvider: FFT): Either[AudioSource.AudioSourceException,Fingerprint] =
    apply(Presets.default, audioSource)

  def apply
  (
    config: Config,
    audioSource: AudioSource
  )(implicit fftProvider: FFT): Either[AudioSource.AudioSourceException,Fingerprint] =
    audioSource.dataStream(config.sampleRate) match {
      case Left(e) =>
        Left(e)
      case Right(audio) =>
        Right(
          apply(
            config,
            audio
          )(fftProvider)
        )
    }

  def apply
  (
    audio: Seq[Short]
  )(implicit fftProvider: FFT): Fingerprint =
    apply(Presets.default, audio)

  def apply
  (
    config: Config,
    audio: Seq[Short]
  )(implicit fftProvider: FFT): Fingerprint = {

    def removeSilence(audio: Seq[Short]): Seq[Short] =
      fingerprinter.removeSilence(config.silenceThreshold, audio)

    def extractFrames(audio: Seq[Short]): Seq[Vector[Double]] =
      fingerprinter.extractFrames(config.framerConfig, audio)

    def extractFeatures(frames: Seq[Vector[Double]]): Seq[Vector[Double]] =
      fingerprinter.extractFeatures(config.chromaConfig, frames)

    def createFingerprint(integral: Image): Fingerprint =
      fingerprinter.createFingerprint(
        config.classifiers,
        config.algorithm,
        if (config.captureDuration) {
          Some(audio.length / config.sampleRate)
        } else {
          None
        },
        integral
      )

    audio.take(config.maxBytes) |>
      removeSilence |>
      extractFrames |>
      extractFeatures |>
      createImage |>
      createFingerprint
  }

  def removeSilence
  (
    silenceThreshold: Short,
    audio: Seq[Short]
  ): Seq[Short] =
    silenceRemover(silenceThreshold, audio)

  def extractFrames
  (
    config: framer.Config,
    audio: Seq[Short]
  )(implicit fftProvider: FFT): Seq[Vector[Double]] =
    framer(config, audio) |>
      fftProvider.apply

  def extractFeatures
  (
    config: chroma.Config,
    frames: Seq[Vector[Double]]
  ): Seq[Vector[Double]] =
    chroma(config, frames) |>
      chromaFilter.apply |>
      chromaNormalizer.apply

  def createImage
  (
    frames: Seq[Vector[Double]]
  ): Image =
    Image(frames).integrate

  def createFingerprint
  (
    classifiers: Classifier.Config,
    algorithm: Int,
    duration: Option[Float],
    image: Image
  ): Fingerprint =
    fingerprintCalculator(classifiers, image) |>
      (Fingerprint(algorithm, duration, _))
}
