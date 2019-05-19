package chromaprint

import cats.effect.IO
import fs2.{Pipe,Stream}
import spire.math.UInt

trait Fingerprinter {

  def apply(audioSource: AudioSource)(implicit fftImpl: FFT): IO[Fingerprint] =
    apply(Config.default, audioSource)

  def apply(config: Config, audioSource: AudioSource)(implicit fftImpl: FFT): IO[Fingerprint] =
    streamFingerprint(config, audioSource)
      .last.compile.toVector.map(_.flatten).map(_(0))

  def streamFingerprint(config: Config, audioSource: AudioSource)(implicit fftImpl: FFT): Stream[IO,Fingerprint] =
    Stream.bracket[IO,Float](
      audioSource.duration
    )(_ => IO.unit) flatMap { duration: Float =>
      streamRaw(config, audioSource) through pipeFingerprint(config.algorithm, duration)
    }

  def streamRaw(config: Config, audioSource: AudioSource)(implicit fftImpl: FFT): Stream[IO,UInt] =
    audioSource.audioStream(config.sampleRate) through pipeRaw(config)

  def pipeFingerprint[F[_]](algorithm: Int, duration: Float)(implicit fftImpl: FFT): Pipe[F,UInt,Fingerprint] =
    _.mapAccumulate[Fingerprint,Fingerprint](Fingerprint(algorithm, duration, Vector.empty)) {
      case (fp, el) =>
        val nextFp = fp.append(el)
        (nextFp, nextFp)
    }.map(_._2)

  def pipeRaw(config: Config)(implicit fftImpl: FFT): Pipe[IO,Short,UInt] =
    audio => (config.maxBytes match {
      case maxBytes if maxBytes > 0 =>
        audio.take(maxBytes)
      case _ =>
        audio
    }).through(SilenceRemover.pipe(config.silenceRemover))
      .through(Framer.pipe(config.framerConfig))
      .through(HammingWindow.pipe(config.frameSize))
      .through(fftImpl.pipe)
      .through(Chroma.pipe(config.chromaConfig))
      .through(ChromaFilter.pipe)
      .through(ChromaNormalizer.pipe)
      .through(IntegralImage.pipe)
      .through(FingerprintCalculator.pipe(config.classifiers))

}

object Fingerprinter extends Fingerprinter
