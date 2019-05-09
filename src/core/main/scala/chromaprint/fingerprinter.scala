package chromaprint

object fingerprinter {

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
    val (inputAudio, duration): (Seq[Short], Option[Float]) =
      if (config.captureDuration) {
        audio.toIndexedSeq |>
          (a => (
            a,
            Some(a.length.toFloat / config.sampleRate)
          ))
      } else {
        (audio, None)
      }
    truncateAudio(
      config.maxBytes,
      config.silenceThreshold,
      inputAudio
    ) |>
      (extractFrames(
        config.framerConfig,
        _
      )) |>
      (extractFeatures(
        config.chromaConfig,
        _
      )) |>
      (Image(_)) |>
      (createFingerprint(
        config.classifiers,
        config.algorithm,
        duration,
        _
      ))
  }

  def truncateAudio
  (
    maxBytes: Int,
    silenceThreshold: Short,
    audio: Seq[Short]
  ): Seq[Short] =
    audio.take(maxBytes) |>
      (silenceRemover(silenceThreshold, _))

  def extractFrames
  (
    config: framer.Config,
    audio: Seq[Short]
  )(implicit fftProvider: FFT): Seq[Vector[Double]] =
    framer(config, audio) |>
      (fftProvider(_))

  def extractFeatures
  (
    config: chroma.Config,
    frames: Seq[Vector[Double]]
  ): Seq[Vector[Double]] =
    chroma(config, frames) |>
      (chromaFilter(_)) |>
      (chromaNormalizer(_))

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
