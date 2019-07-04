package chromaprint

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import java.net.{MalformedURLException, URL}
import javax.sound.sampled._
import cats.effect._
import fs2.{Chunk, Pipe, Pure, Stream}

object AudioSource {

  val targetSampleSize: Int = 16
  val targetChannels: Int = 1
  val targetFrameSize: Int = 2

  def targetFormat(sampleRate: Int): AudioFormat =
    new AudioFormat(
      AudioFormat.Encoding.PCM_SIGNED,
      sampleRate,
      targetSampleSize,
      targetChannels,
      targetFrameSize,
      sampleRate,
      false
    )

  def bytePairToShort(big: Byte, little: Byte): Short =
    ((0xff & big) << 8 | (0xff & little)).toShort

  def pipeBytePairs[F[_]]: Pipe[F,Byte,Short] =
    _.mapAccumulate[Option[Byte],Option[Short]](None){
      case (None, little) =>
        (Some(little), None)
      case (Some(little), big) =>
        (None, Some(bytePairToShort(big, little)))
    }
      .map(_._2).
      unNone

  def defaultChunkSize(format: AudioFormat): Int =
    (format.getSampleRate * format.getSampleSizeInBits / 8).toInt max 11025

  def audioInputStreamToByteStream(audioInputStream: IO[AudioInputStream]): Stream[IO,Byte] =
    Stream.bracket{
      audioInputStream
    }(stream => IO(stream.close())).flatMap{ stream =>
      audioInputStreamToByteStream(IO.pure(stream), defaultChunkSize(stream.getFormat))
    }

  def audioInputStreamToByteStream(audioInputStream: IO[AudioInputStream], chunkSize: Int): Stream[IO,Byte] =
    Stream.bracket[IO,AudioInputStream] {
      audioInputStream
    }(
      stream => IO {
        stream.close()
      }
    ).flatMap{ stream =>
      Stream.iterateEval(Some(Stream[Pure,Byte]()): Option[Stream[Pure,Byte]])(_ => IO {
        val arr = new Array[Byte](chunkSize)
        val n = stream.read(arr)
        if (n >= 0) {
          Some(Stream.chunk[Pure,Byte](Chunk.array(arr.take(n))))
        } else {
          None
        }
      })
    }
      .unNoneTerminate.
      flatten

  def fromFile(file: File): AudioSystemSource =
    AudioFileSource(file)

  implicit def apply(file: File): AudioSystemSource =
    fromFile(file)

  def fromUrl(url: URL): AudioSystemSource =
    AudioURLSource(url)

  implicit def apply(url: URL): AudioSystemSource =
    fromUrl(url)

  def fromInputStream(stream: InputStream): AudioSystemSource =
    AudioInputStreamSource(stream)

  implicit def apply(stream: InputStream): AudioSystemSource =
    fromInputStream(stream)

  def fromString(str: String): Either[AudioSource.AudioSourceException,AudioSource] = {
    val file = new File(str)
    if (file.isFile) {
      Right(apply(file))
    } else {
      try {
        Right(apply(new URL(str)))
      } catch {
        case _: MalformedURLException =>
          Left(new AudioSourceException(s"Invalid audio source: '$str'"))
      }
    }
  }

  def apply(str: String): Either[AudioSource.AudioSourceException,AudioSource] =
    fromString(str)

  class AudioSourceException(message: String) extends Exception(message)
  class DurationException(message: String) extends AudioSourceException(message)
  class ConversionException(message: String) extends AudioSourceException(message)
  class ResamplingException(message: String) extends AudioSourceException(message)

  abstract class AudioSystemSource extends AudioSource() {

    override def duration: IO[Float] = audioFileFormat.flatMap(fileFormat => IO {
      Option(fileFormat.properties()).
        map(_.get("duration")).
        map(_.toString.toFloat).
        filter(_ > 0F).
        map(_ / 1000000).
        getOrElse({
          fileFormat.getFormat.getSampleRate match {
            case sampleRate if sampleRate > 0F =>
              fileFormat.getFrameLength.toFloat / sampleRate
            case _ =>
              0F
          }
        })
    })

    val audioFileFormat: IO[AudioFileFormat]

    protected def acquireAudioInputStream: IO[AudioInputStream]

    def targetFormat(sampleRate: Int): AudioFormat =
      AudioSource.targetFormat(sampleRate)

    protected def acquireIntermediateAudioInputStream(sampleRate: Int): IO[AudioInputStream] =
      acquireAudioInputStream map { stream =>
        if (!AudioSystem.isConversionSupported(targetFormat(sampleRate), stream.getFormat)) {
          if (AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, stream.getFormat)) {
            AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, stream)
          } else {
            throw new ConversionException("Cannot convert input audio")
          }
        } else {
          stream
        }
      }

    protected def acquireResampledAudioInputStream(sampleRate: Int): IO[AudioInputStream] =
      acquireIntermediateAudioInputStream(sampleRate) map { intermediate =>
        val intermediateFormat: AudioFormat = intermediate.getFormat
        val finalFormat = targetFormat(sampleRate)
        if (intermediateFormat.getChannels != 1 || intermediateFormat.getSampleRate != sampleRate) {
          if (!AudioSystem.isConversionSupported(finalFormat, intermediateFormat)) {
            throw new ResamplingException(s"Cannot resample audio from $intermediateFormat to $finalFormat")
          } else {
            AudioSystem.getAudioInputStream(
              finalFormat,
              intermediate
            )
          }
        } else {
          intermediate
        }
      }

    def audioByteStream(sampleRate: Int): Stream[IO,Byte] =
      audioInputStreamToByteStream(acquireResampledAudioInputStream(sampleRate))

    def audioStream(sampleRate: Int): Stream[IO,Short] =
      audioByteStream(sampleRate) through pipeBytePairs

  }

  case class AudioFileSource(file: File) extends AudioSystemSource {

    def name: String =
      file.getName

    val audioFileFormat: IO[AudioFileFormat] =
      IO(AudioSystem.getAudioFileFormat(file))

    protected def acquireAudioInputStream: IO[AudioInputStream] =
      IO(AudioSystem.getAudioInputStream(file))

    protected def acquireRawInputStream: IO[InputStream] =
      IO(new BufferedInputStream(new FileInputStream(file)))

  }

  case class AudioURLSource(url: URL) extends AudioSystemSource {

    def name: String =
      url.toString

    val audioFileFormat: IO[AudioFileFormat] =
      IO(AudioSystem.getAudioFileFormat(url))

    def acquireAudioInputStream: IO[AudioInputStream] =
      IO(AudioSystem.getAudioInputStream(url))
  }

  case class AudioInputStreamSource(stream: InputStream) extends AudioSystemSource {

    def name: String =
      toString

    val audioFileFormat: IO[AudioFileFormat] =
      IO(AudioSystem.getAudioFileFormat(stream))

    def acquireAudioInputStream: IO[AudioInputStream] =
      stream match {
        case s: AudioInputStream =>
          IO.pure(s)
        case _ =>
          IO(AudioSystem.getAudioInputStream(stream))
      }
  }

}

abstract class AudioSource {

  def name: String

  val defaultRawChunkSize: Int = 32

  def duration: IO[Float]

  def audioStream(sampleRate: Int): Stream[IO,Short]
}
