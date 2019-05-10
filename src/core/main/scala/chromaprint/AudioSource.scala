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

  def convertBytes(bytes: Seq[Byte]): Seq[Short] =
    bytes.grouped(2).map(p => bytePairToShort(p(1), p(0))).toSeq

  implicit def apply(file: File): AudioSystemSource =
    new AudioSystemSource {
      def name: String =
        file.getName
      def audioInputStream: Either[AudioSourceException,AudioInputStream] =
        catchAudioSystemException(() => AudioSystem.getAudioInputStream(file))
    }

  implicit def apply(url: URL): AudioSystemSource =
    new AudioSystemSource {
      def name: String =
        url.toString
      def audioInputStream: Either[AudioSourceException,AudioInputStream] =
        catchAudioSystemException(() => AudioSystem.getAudioInputStream(url))
    }

  implicit def apply(stream: InputStream): AudioSystemSource =
    new AudioSystemSource {
      def name: String =
        toString
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

  def name: String

  def dataStream(sampleRate: Int): Either[AudioSource.AudioSourceException,Seq[Short]]
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

  def dataStream: Either[AudioSourceException,Seq[Short]] =
    dataStream(Config.Defaults.sampleRate)

  def dataStream(sampleRate: Int): Either[AudioSourceException,Seq[Short]] = {
    resampledAudioStream(sampleRate) match {
      case Left(e) =>
        Left(e)
      case Right(audio) =>
        def read: Option[Seq[Byte]] = {
          val arr = new Array[Byte](2)
          val n = audio.read(arr)
          if (n < 2) {
            audio.close()
            None
          } else {
            Some(arr.take(2))
          }
        }
        Right(
          convertBytes(
            Stream.continually(read)
              .takeWhile(_.isDefined)
              .flatMap(_.getOrElse(Seq.empty[Byte]))
          )
        )
    }
  }

  def preload(): AudioSource =
    new AudioSource {
      val name: String =
        AudioSystemSource.this.name
      val dataStream: Either[AudioSourceException,Seq[Short]] =
        AudioSystemSource.this.dataStream match {
          case Left(e) =>
            Left(e)
          case Right(s) =>
            Right(s.toStream.force)
        }
      def dataStream(sampleRate: Int): Either[AudioSourceException,Seq[Short]] =
        sampleRate match {
          case Config.Defaults.sampleRate =>
            dataStream
          case _ =>
            AudioSystemSource.this.dataStream(sampleRate)
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
