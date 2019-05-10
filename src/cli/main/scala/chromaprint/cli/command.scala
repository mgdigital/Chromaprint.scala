package chromaprint.cli

import chromaprint._
import chromaprint.acoustid.lookup

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object command {

  object Params {

    val default: Params =
      Params(
        Config.default,
        preload = true,
        showCompressed = true,
        showRaw = false,
        showHash = false,
        None
      )
  }

  final case class Params
  (
    config: Config,
    preload: Boolean,
    showCompressed: Boolean,
    showRaw: Boolean,
    showHash: Boolean,
    acoustid: Option[lookup.Config]
  )

  object Args {

    val default: Args =
      Args(Params.default, Vector.empty)
  }

  final case class Args
  (
    params: Params,
    sources: Vector[AudioSource]
  ) {

    def withParams(params: Params): Args =
      copy(params = params)

    def withParams(u: Params => Params): Args =
      withParams(u(params))

    def config: Config =
      params.config

    def withConfig(config: Config): Args =
      copy(params = params.copy(config = config))

    def withConfig(u: Config => Config): Args =
      withConfig(u(config))

    def withSource(source: AudioSource): Args =
      copy(sources = sources :+ source)
  }

  def apply(args: Vector[String])(implicit fftProvider: FFT): Unit =
    parse(args).foreach(args => apply(args.params, args.sources))

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  def apply(params: Params, sources: Seq[AudioSource])(implicit fftProvider: FFT): Unit = {
    if (sources.isEmpty) {
      Console.err.println("No audio sources were specified!")
    } else {
      val useSources = if (params.preload) {
        preloadedSources(sources)
      } else {
        sources
      }
      val (secondsElapsed, results) = timed(() => {
        val resultFutures = useSources.map { source => resultFuture(params.config, source).map(r => (source.name, r))}
        Await.result(Future.sequence(resultFutures), atMost = 60.seconds)
      })
      Console.out.println(s"Generated ${results.count(_._2.isRight)} fingerprints in ${secondsElapsed}s")
      results.foreach { t => handleResult(params, t._1, t._2)}
    }
  }

  private def preloadedSources(sources: Seq[AudioSource]): Seq[AudioSource] =
    sources.foldLeft(Vector.empty[AudioSource]) { (sources, next) =>
      val preloaded = next match {
        case s: AudioSystemSource =>
          Console.out.println(s"Preloading ${next.name}...")
          val (secondsElapsed, preloaded) = timed(s.preload)
          Console.out.println(s"Preloaded ${preloaded.name} in ${secondsElapsed}s")
          preloaded
        case s =>
          s
      }
      sources :+ preloaded
    }

  private def resultFuture
  (config: Config, source: AudioSource)
  (implicit fftProvider: FFT): Future[Either[AudioSource.AudioSourceException,Fingerprint]] =
    Future {
      val name = source.name
      Console.out.println(s"Fingerprinting $name")
      val (secondsElapsed, result) = timed(() => fingerprinter(config, source))
      Console.out.println(s"Done fingerprinting $name in ${secondsElapsed}s")
      result
    }

  private def handleResult
  (params: Params, name: String, result: Either[AudioSource.AudioSourceException,Fingerprint]): Unit =
    result match {
      case Left(e) =>
        Console.err.println(s"Error creating fingerprint for $name: ${e.getMessage}")
      case Right(fp) =>
        handleFingerprint(params, name, fp)
    }

  private def handleFingerprint
  (params: Params, name: String, fingerprint: Fingerprint): Unit = {

    Console.out.println(s"Fingerprint for $name:")
    Console.out.println(s"Duration: ${fingerprint.duration}")
    if (params.showRaw) {
      Console.out.println(s"Fingerprint (raw): ${fingerprint.data}")
    }
    if (params.showCompressed) {
      Console.out.println(s"Fingerprint (compressed): ${fingerprint.compressed}")
    }
    if (params.showHash) {
      Console.out.println(s"Hash: ${fingerprint.hash}")
    }
    params.acoustid foreach (doAcoustIDLookup(_, name, fingerprint))
  }

  private def doAcoustIDLookup
  (config: lookup.Config, name: String, fingerprint: Fingerprint): Unit =
    lookup(config, lookup.Params(fingerprint)) match {
      case Left(e) =>
        Console.err.println(s"AcoustID lookup failed: ${e.getMessage}")
      case Right(response) =>
        response match {
          case r: lookup.ErrorResponse =>
            Console.err.println("AcoustID lookup failed: " + r.message)
          case r: lookup.OKResponse =>
            Console.out.println(s"AcoustID response received for $name:")
            if (r.results.isEmpty) {
              Console.out.println("no results were found")
            } else {
              r.results.zipWithIndex.foreach { t =>
                val (result, i) = t
                Console.out.println(s"${result.id}: Result ${i + 1} with score ${result.score}:")
                result.recordings.foreach { recording =>
                  Console.out.println(
                    s"${recording.id}: '${recording.title}' by '${recording.artists.map(_.name).mkString("; ")}'"
                  )
                }
              }
            }
        }
    }

  private def timed[T](fn: () => T): (Float, T) = {
    val startTime = System.currentTimeMillis()
    val result = fn()
    val timeElapsed = System.currentTimeMillis() - startTime
    val secondsElapsed = timeElapsed.toFloat / 1000
    (secondsElapsed, result)
  }
}
