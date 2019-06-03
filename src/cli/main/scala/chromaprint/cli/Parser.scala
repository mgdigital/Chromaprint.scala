package chromaprint.cli

import cats.implicits._
import com.monovore.decline._
import cats.data.{NonEmptyList, Validated}
import chromaprint.{AudioSource, Config, Presets}
import chromaprint.acoustid.{Config => AcoustIDConfig}

object Parser {

  import Config.Defaults

  val algorithm: Opts[Int] = Opts.option[Int](
    long = "algorithm",
    short = "a",
    help = s"Algorithm to use (default: ${Defaults.algorithm})"
  ).
    withDefault(Defaults.algorithm).
    validate("Unknown algorithm.")(Presets.exists)

  val maxDuration: Opts[Int] = Opts.option[Int](
    long = "length",
    short = "l",
    help = s"Restrict the duration in seconds of the processed input audio (default: ${Defaults.maxDuration})"
  ).
    withDefault(Defaults.maxDuration)

  val sampleRate: Opts[Int] = Opts.option[Int](
    long = "rate",
    short = "r",
    help = s"Sample rate for audio conversion (default: ${Defaults.sampleRate})"
  ).
    withDefault(Defaults.sampleRate)

  val frameSize: Opts[Int] = Opts.option[Int](
    long = "frame-size",
    short = "f",
    help = s"Split incoming audio into chunks of this many bytes (default: ${Defaults.frameSize})"
  ).
    withDefault(Defaults.frameSize)

  val overlap: Opts[Int] = Opts.option[Int](
    long = "overlap",
    short = "o",
    help = s"Overlap between frames in bytes (default: ${Defaults.overlap})"
  ).
    withDefault(Defaults.overlap)

  val showRaw: Opts[Boolean] = Opts.flag(
    long = "raw",
    short = "w",
    help = "Show raw (default: false)"
  ).orFalse

  val showCompressed: Opts[Boolean] = Opts.flag(
    long = "compressed",
    short = "c",
    help = "Show compressed (default: true)"
  ).orTrue

  val showHash: Opts[Boolean] = Opts.flag(
    long = "hash",
    short = "h",
    help = "Show hash (default: false)"
  ).orFalse

  val repetitions: Opts[Int] = Opts.option[Int](
    long = "number",
    short = "n",
    help = "Number of repetitions, helpful for benchmarking (default: 1)"
  ).withDefault(1)

  val acoustidClient: Opts[Option[String]] = Opts.option[String](
    long = "acoustid-client",
    short = "i",
    help = "AcoustID client ID (if set, AcoustID matches will be fetched)"
  ).
    map(Some(_)).withDefault(None)

  val sources: Opts[NonEmptyList[AudioSource]] =
    Opts.arguments[String]("sources").mapValidated[NonEmptyList[AudioSource]](
      strSources => strSources.foldLeft(Validated.Valid(Vector.empty[AudioSource]
      ): Validated[String,Vector[AudioSource]]){
        (result, next) =>
          result match {
            case v: Validated.Valid[Vector[AudioSource]] =>
              AudioSource.fromString(next) match {
                case Right(source) =>
                  Validated.Valid[Vector[AudioSource]](v.a :+ source)
                case Left(e) =>
                  Validated.Invalid[String](e.getMessage)
              }
            case i: Validated.Invalid[String] => i
          }
      } match {
        case i: Validated.Invalid[String] =>
          Validated.invalidNel[String,NonEmptyList[AudioSource]](i.e)
        case v: Validated.Valid[Vector[AudioSource]] =>
          NonEmptyList.fromList(v.a.toList) match {
            case Some(nel) =>
              Validated.validNel[String,NonEmptyList[AudioSource]](nel)
            case _ =>
              Validated.invalidNel[String,NonEmptyList[AudioSource]]("No audio sources found")
          }
      })

  val config: Opts[Config] = (algorithm, maxDuration, sampleRate, frameSize, overlap).mapN{
    (algorithm, maxDuration, sampleRate, frameSize, overlap) =>
      Config.default.copy(
        algorithm = algorithm,
        maxDuration = maxDuration,
        sampleRate = sampleRate,
        frameSize = frameSize,
        overlap = overlap
      )
  }

  val params: Opts[Args.Params] = (config, showCompressed, showRaw, showHash, repetitions, acoustidClient).mapN{
    (config, showCompressed, showRaw, showHash, repetitions, acoustidClient) =>
      Args.Params(
        config,
        showCompressed,
        showRaw,
        showHash,
        acoustidClient.map(AcoustIDConfig(_)),
        repetitions
      )
  }

  val args: Opts[Args] = (params, sources).mapN{
    (params, sources) =>
      Args(params, sources.toList)
  }
}
