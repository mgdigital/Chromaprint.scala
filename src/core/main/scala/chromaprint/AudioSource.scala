package chromaprint

import scala.language.implicitConversions

import java.io.{File, IOException, InputStream}
import java.net.URL

import javax.sound.sampled.AudioFormat.Encoding
import javax.sound.sampled._

object AudioSource {

  val targetSampleSize: Int = 16
  val targetChannels: Int = 1
  val targetFrameSize: Int = 2

  def targetFormat(sampleRate: Int): AudioFormat =
    new AudioFormat(
      Encoding.PCM_SIGNED,
      sampleRate,
      targetSampleSize,
      targetChannels,
      targetFrameSize,
      sampleRate,
      false
    )

  def bytePairToShort(big: Byte, little: Byte): Short =
    ((0xff & big) << 8 | (0xff & little)).toShort

  implicit def apply(file: File): AudioSystemSource =
    new AudioSystemSource {
      def audioInputStream: Either[AudioSourceException,AudioInputStream] =
        catchAudioSystemException(() => AudioSystem.getAudioInputStream(file))
    }

  implicit def apply(url: URL): AudioSystemSource =
    new AudioSystemSource {
      def audioInputStream: Either[AudioSourceException,AudioInputStream] =
        catchAudioSystemException(() => AudioSystem.getAudioInputStream(url))
    }

  implicit def apply(stream: InputStream): AudioSystemSource =
    new AudioSystemSource {
      def audioInputStream: Either[AudioSourceException,AudioInputStream] =
        stream match {
          case s: AudioInputStream =>
            Right(s)
          case _ =>
            catchAudioSystemException(() => AudioSystem.getAudioInputStream(stream))
        }
    }

  class AudioSourceException(message: String) extends Exception(message)
  class ConversionException(message: String) extends AudioSourceException(message)
  class ResamplingException(message: String) extends AudioSourceException(message)
  class InputException(message: String) extends AudioSourceException(message)

}

trait AudioSource {

  def dataStream(sampleRate: Int): Either[AudioSource.AudioSourceException,Stream[Short]]
}

trait AudioSystemSource extends AudioSource {

  import AudioSource._

  def audioInputStream: Either[AudioSourceException,AudioInputStream]

  lazy val sourceFormat: Either[AudioSourceException,AudioFormat] =
    audioInputStream match {
      case Left(e) =>
        Left(e)
      case Right(s) =>
        Right(s.getFormat)
    }

  def resampledAudioStream: Either[AudioSourceException,AudioInputStream] =
    resampledAudioStream(Config.Defaults.sampleRate)

  def resampledAudioStream(sampleRate: Int): Either[AudioSourceException,AudioInputStream] =
    audioInputStream match {
      case Left(e) =>
        Left(e)
      case Right(s) =>
        val targetFormat: AudioFormat =
          AudioSource.targetFormat(sampleRate)
        val needsIntermediateConversion: Boolean =
          !AudioSystem.isConversionSupported(targetFormat, s.getFormat)
        val intermediateAudioInputStream: Either[ConversionException,AudioInputStream] =
          if (needsIntermediateConversion) {
            if (AudioSystem.isConversionSupported(Encoding.PCM_SIGNED, s.getFormat)) {
              Right(AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, s))
            } else {
              Left(new ConversionException("Cannot convert input audio"))
            }
          } else {
            Right(s)
          }
        intermediateAudioInputStream match {
          case Left(e) =>
            Left(e)
          case Right(intermediate) =>
            val intermediateFormat: AudioFormat =
              intermediate.getFormat
            val needsFinalConversion: Boolean =
              intermediateFormat.getChannels != 1 || intermediateFormat.getSampleRate != sampleRate
            if (needsFinalConversion) {
              if (!AudioSystem.isConversionSupported(targetFormat, intermediateFormat)) {
                Left(new ResamplingException(s"Cannot resample audio from $intermediateFormat to $targetFormat"))
              } else {
                Right(AudioSystem.getAudioInputStream(
                  targetFormat,
                  intermediate
                ))
              }
            } else {
              Right(intermediate)
            }

        }
    }

  def dataStream: Either[AudioSourceException,Stream[Short]] =
    dataStream(Config.Defaults.sampleRate)

  def dataStream(sampleRate: Int): Either[AudioSourceException,Stream[Short]] = {
    resampledAudioStream(sampleRate) match {
      case Left(e) =>
        Left(e)
      case Right(audio) =>
        def read: Option[(Byte, Byte)] = {
          val arr = new Array[Byte](2)
          val n = audio.read(arr)
          if (n < 2) {
            audio.close()
            None
          } else {
            Some(arr(1), arr(0))
          }
        }
        Right(
          Stream.continually(read)
            .takeWhile(_.isDefined)
            .flatten
            .map(p => bytePairToShort(p._1, p._2))
        )
    }
  }

  protected def catchAudioSystemException[T](fn: () => T): Either[AudioSourceException,T] =
    try {
      Right(fn())
    } catch {
      case e: IOException =>
        Left(new InputException(e.getMessage))
      case e: UnsupportedAudioFileException =>
        Left(new ConversionException(e.getMessage))
    }

}
