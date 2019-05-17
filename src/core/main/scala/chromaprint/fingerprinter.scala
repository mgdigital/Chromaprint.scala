package chromaprint

import cats.effect.IO
import fs2.{Pipe,Stream}
import spire.math.UInt

trait fingerprinter {

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

  def pipeRaw[F[_]](config: Config)(implicit fftImpl: FFT): Pipe[F,Short,UInt] =
    _.take(config.maxBytes)
      .through(silenceRemover.pipe(config.silenceRemover))
      .through(framer.pipe(config.framerConfig))
      .through(hammingWindow.pipe(config.frameSize))
      .through(fftImpl.pipe)
      .through(chroma.pipe(config.chromaConfig))
      .through(chromaFilter.pipe)
      .through(chromaNormalizer.pipe)
      .through(integralImage.pipe)
      .through(fingerprintCalculator.pipe(config.classifiers))

}

object fingerprinter extends fingerprinter
