package chromaprint.cli

import chromaprint._
import chromaprint.acoustid.lookup

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object Command {

  object Params {

    val default: Params =
      Params(
        Config.default,
        showCompressed = true,
        showRaw = false,
        showHash = false,
        None
      )
  }

  final case class Params
  (
    config: Config,
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

  def apply(args: Vector[String])(implicit fftImpl: FFT): Unit =
    Parser(args).foreach(args => apply(args.params, args.sources))

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  def apply(params: Params, sources: Seq[AudioSource])(implicit fftImpl: FFT): Unit = {
    if (sources.isEmpty) {
      Console.err.println("No audio sources were specified!")
    } else {
      val (secondsElapsed, results) = timed(() => {
        sources.map { source =>
          Fingerprinter(
            params.config,
            source
          ).unsafeToFuture().map(r => (source.name, r))
        }.map(Await.result(_, atMost = 30.seconds))
      })
      Console.out.println(s"Generated ${results.length} fingerprints in ${secondsElapsed}s")
      results.foreach { t => handleFingerprint(params, t._1, t._2)}
    }
  }

  private def handleFingerprint
  (params: Params, name: String, fingerprint: Fingerprint): Unit = {

    Console.out.println(s"Fingerprint for $name:")
    Console.out.println(s"Duration: ${fingerprint.trackDuration}")
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
