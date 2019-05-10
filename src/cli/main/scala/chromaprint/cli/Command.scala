package chromaprint.cli

import java.io.File
import java.nio.file.Files
import java.util.concurrent.locks.ReentrantLock

import chromaprint._
import chromaprint.acoustid.lookup

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object Command {

  object Params {

    val default: Params =
      Params(
        Config.default,
        Vector.empty,
        preload = true,
        showCompressed = true,
        showRaw = false,
        showHash = false,
        None
      )
  }

  final case class Params
  (
    configuration: Config,
    sources: Vector[AudioSource],
    preload: Boolean,
    showCompressed: Boolean,
    showRaw: Boolean,
    showHash: Boolean,
    acoustid: Option[lookup.Config]
  )

  import scopt._

  val builder: OParserBuilder[Params] = OParser.builder[Params]

  val parser: OParser[Unit,Params] = {
    import builder._
    OParser.sequence(
      programName("fpcalc"),
      head("fpcalc", "0.1"),
      opt[Int]('a', "algorithm")
        .validate(x =>
          if (Presets.exists(x)) {
            success
          } else {
            failure(s"algorithm $x is unknown")
          })
        .text("algorithm to use (default 2)")
        .action((x, params) => params.copy(configuration = Presets(x))),
      opt[Int]('l', "length")
        .action((x, params) => params.copy(configuration = params.configuration.copy(maxDuration = x))),
      opt[Int]('o', "overlap")
        .action((x, params) => params.copy(configuration = params.configuration.copy(overlap = x))),
      opt[Int]('r', "rate")
        .action((x, params) => params.copy(configuration = params.configuration.copy(sampleRate = x))),
      opt[Boolean]('p', "preload")
        .text("preload audio into memory before fingerprinting")
        .action((x, params) => params.copy(preload = x)),
      opt[Boolean]("raw")
        .action((x, params) => params.copy(showRaw = x)),
      opt[Boolean]("compressed")
        .action((x, params) => params.copy(showCompressed = x)),
      opt[Boolean]("hash")
        .action((x, params) => params.copy(showHash = x)),
      opt[Boolean]('a', "acoustid")
        .text("lookup matches from acoustid")
        .action((x, params) => params.copy(acoustid = if (x) Some(lookup.Config()) else None)),
      opt[String]("acoustid-client")
        .text("acoustid client id")
        .action((x, params) => params.copy(
          acoustid = params.acoustid.orElse(Some(lookup.Config())).map(_.copy(clientId = Some(x)))
        )),
      arg[File]("<file>...")
        .unbounded()
        .action((x, c) => c.copy(sources = c.sources :+ AudioSource(x)))
        .text("files to fingerprint")
    )
  }

  def apply(args: Vector[String])(implicit fftProvider: FFT): Unit =
    OParser.parse(parser, args, Params.default).foreach{ params =>
      implicit val executionContext: ExecutionContext = ExecutionContext.global
      val outputLock = new ReentrantLock()
      if (params.sources.isEmpty) {
        Console.err.println("No audio sources were specified!")
      } else {
        val sources =
          if (params.preload) {
            params.sources.foldLeft(Vector.empty[AudioSource]){ (sources, next) =>
              Console.out.println(s"Preloading ${next.name}...")
              val preloaded = next match {
                case s: AudioSystemSource =>
                  s.preload()
                case s =>
                  s
              }
              Console.out.println(s"Preloaded ${preloaded.name}")
              sources :+ preloaded
            }
          } else {
            params.sources
          }
        val startTime = System.currentTimeMillis()
        val resultFutures = sources.map{ source =>
          Future {
            val name = source.name
            Console.out.println(s"Fingerprinting $name")
            val startTime = System.currentTimeMillis()
            val result = fingerprinter(params.configuration, source)
            val timeElapsed = System.currentTimeMillis() - startTime
            val secondsElapsed = timeElapsed.toFloat / 1000
            Console.out.println(s"Done fingerprinting $name in ${secondsElapsed}s")
            (name, result)
          }
        }
        val results = Await.result(Future.sequence(resultFutures), atMost = 60.seconds)
        val timeElapsed = System.currentTimeMillis() - startTime
        val secondsElapsed = timeElapsed.toFloat / 1000
        Console.out.println(s"Generated ${results.count(_._2.isRight)} fingerprints in ${secondsElapsed}s")
        results.foreach { t =>
          val (name, result) = t
          discard(result match {
            case Left(e) =>
              Console.err.println(s"Error creating fingerprint: ${e.getMessage}")
            case Right(fp) =>
              Console.out.println(s"Fingerprint for $name:")
              Console.out.println(s"Duration: ${fp.duration}")
              if (params.showRaw) {
                Console.out.println(s"Fingerprint (raw): ${fp.data}")
              }
              if (params.showCompressed) {
                Console.out.println(s"Fingerprint (compressed): ${fp.compressed}")
              }
              if (params.showHash) {
                Console.out.println(s"Hash: ${fp.hash}")
              }
              params.acoustid match {
                case Some(acoustid) =>
                  lookup(acoustid, lookup.Params(fp)) match {
                    case Right(response) =>
                      response match {
                        case r: lookup.OKResponse =>
                          Console.out.println("AcoustID response received")
                          if (r.results.isEmpty) {
                            Console.out.println("no results were found")
                          } else {
                            r.results.groupBy(_.recordings(0).id).map(_._2(0)).zipWithIndex.foreach { t =>
                              val (result, i) = t
                              Console.out.println(s"Result ${i + 1} with score ${result.score}:")
                              result.recordings.foreach { recording =>
                                Console.out.println(
                                  s"${recording.id} '${recording.title}' by '${recording.artists.map(_.name).mkString("; ")}'"
                                )
                              }
                            }
                          }
                        case r: lookup.ErrorResponse =>
                          Console.err.println("AcoustID lookup failed: " + r.message)
                      }
                    case Left(e) =>
                      Console.err.println(s"AcoustID lookup failed: ${e.getMessage}")
                  }
                case None =>
                  Unit
              }
          })
        }
      }
    }
}
