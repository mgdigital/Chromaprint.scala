package chromaprint

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import java.net.{MalformedURLException, URL}
import javax.sound.sampled._
import scala.concurrent.ExecutionContext
import cats.effect._
import cats.implicits._
import fs2.{Chunk, Pipe, Pure, Stream}

object AudioSource {

  implicit val executionContext: ExecutionContext = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

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
      .map(_._2)
      .unNone

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
      .unNoneTerminate
      .flatten

  // @TODO: Check if this works for all formats
  def readHeaderDuration(stream: Stream[IO,Byte]): IO[Float] =
    stream.drop(18).chunkN(8, allowFewer = false).take(1).compile.toVector.map{
      case Vector(c) =>
        val sampleRate = readSampleRate(c(0), c(1), c(2))
        val numSamples = readTotalNumberOfSamples(c(3), c(4), c(5), c(6), c(7))
        (numSamples.toDouble / sampleRate).toFloat
      case _ =>
        throw new DurationException("Could not extract duration from headers")
    }

  def readSampleRate(b1: Byte, b2: Byte, b3: Byte): Long =
    ((b1.toLong & 0xff) << 12) +
      ((b2.toLong & 0xff) << 4) +
      (((b3.toLong & 0xff) & 0xF0) >>> 4)

  def readTotalNumberOfSamples(b1: Byte, b2: Byte, b3: Byte, b4: Byte, b5: Byte): Long =
    (b5.toLong & 0xff) +
      ((b4.toLong & 0xff) << 8) +
      ((b3.toLong & 0xff) << 16) +
      ((b2.toLong & 0xff) << 24) +
      (((b1.toLong & 0xff) & 0x0f) << 32)

  def estimateAudioInputStreamDuration(audioInputStream: AudioInputStream): Option[Float] =
    audioInputStream.getFrameLength match {
      case frameLength if frameLength > 0 =>
        audioInputStream.getFormat.getSampleRate match {
          case sourceSampleRate if sourceSampleRate > 0 =>
            Some((frameLength.toDouble / sourceSampleRate / 2).toFloat)
          case _ =>
            None
        }
      case _ =>
        None
    }

  implicit def apply(file: File): AudioSystemSource =
    new AudioSystemSource {
      def name: String =
        file.getName

      protected def acquireAudioInputStream: IO[AudioInputStream] =
        IO.shift *> IO(AudioSystem.getAudioInputStream(file))

      protected def acquireRawInputStream: IO[InputStream] =
        IO.shift *> IO(new BufferedInputStream(new FileInputStream(file)))

      override def rawByteStream(chunkSize: Int): Stream[IO,Byte] =
        Stream.bracket[IO,InputStream](acquireRawInputStream)(stream => IO { stream.close() })
          .flatMap[IO,Option[Stream[Pure,Byte]]](s => {
            val arr = new Array[Byte](chunkSize)
            val n = s.read(arr)
            if (n < 0) {
              Stream(None)
            } else {
              Stream[Pure, Option[Stream[Pure,Byte]]](Some(Stream.chunk(Chunk.array(arr.take(n)))))
            }
          }).unNoneTerminate.flatten
    }

  implicit def apply(url: URL): AudioSystemSource =
    new AudioSystemSource {
      def name: String =
        url.toString
      def acquireAudioInputStream: IO[AudioInputStream] =
        IO.shift *> IO(AudioSystem.getAudioInputStream(url))
    }

  def apply(str: String): Either[AudioSource.AudioSourceException,AudioSource] = {
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

  implicit def apply(stream: InputStream): AudioSystemSource =
    new AudioSystemSource {
      def name: String =
        toString
      def acquireAudioInputStream: IO[AudioInputStream] =
        stream match {
          case s: AudioInputStream =>
            IO.pure(s)
          case _ =>
            IO {
              AudioSystem.getAudioInputStream(stream)
            }
        }
    }

  class AudioSourceException(message: String) extends Exception(message)
  class DurationException(message: String) extends AudioSourceException(message)
  class ConversionException(message: String) extends AudioSourceException(message)
  class ResamplingException(message: String) extends AudioSourceException(message)

}

trait AudioSource {

  import AudioSource._

  def name: String

  val defaultRawChunkSize: Int = 32

  def rawByteStream: Stream[IO,Byte] =
    rawByteStream(defaultRawChunkSize)

  def rawByteStream(chunkSize: Int): Stream[IO,Byte]

  def duration: IO[Float] =
    readHeaderDuration(rawByteStream)

  def audioStream(sampleRate: Int): Stream[IO,Short]
}

trait AudioSystemSource extends AudioSource {

  import AudioSource._

  protected def acquireAudioInputStream: IO[AudioInputStream]

  def rawByteStream(chunkSize: Int): Stream[IO,Byte] =
    audioInputStreamToByteStream(acquireAudioInputStream, chunkSize)

  def targetFormat(sampleRate: Int): AudioFormat =
    AudioSource.targetFormat(sampleRate)

  protected def acquireIntermediateAudioInputStream(sampleRate: Int): IO[AudioInputStream] =
    IO.shift *> acquireAudioInputStream map { stream =>
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
    IO.shift *> acquireIntermediateAudioInputStream(sampleRate) map { intermediate =>
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
