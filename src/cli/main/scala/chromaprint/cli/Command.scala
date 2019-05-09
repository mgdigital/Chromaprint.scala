package chromaprint.cli

import java.io.File

import chromaprint.{Config,Presets,AudioSource,FFT,fingerprinter}
import chromaprint.acoustid.lookup

object Command {

  final case class Params
  (
    configuration: Config = Presets.default,
    sources: Vector[AudioSource] = Vector.empty,
    acoustid: Option[lookup.Config] = None
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
    OParser.parse(parser, args, Params()).foreach{ params =>
      params.sources.foreach { source =>
        Console.out.println("Fingerprinting...")
        val startTime = System.currentTimeMillis()
        fingerprinter(params.configuration, source) match {
          case Right(fp) =>
            val raw = fp.data.mkString(",")
            val compressed = fp.compressed
            val hash = fp.hash
            val timeElapsed = System.currentTimeMillis() - startTime
            val secondsElapsed = timeElapsed.toFloat / 1000
            Console.out.println(s"Created fingerprint in ${secondsElapsed}s")
            Console.out.println(s"Duration: ${fp.duration}")
            Console.out.println(s"Fingerprint (raw): $raw")
            Console.out.println(s"Fingerprint (compressed): $compressed")
            Console.out.println(s"Hash: $hash")
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
                          r.results.groupBy(_.recordings.head.id).map(_._2.head).zipWithIndex.foreach{ t =>
                            val (result, i) = t
                            Console.out.println(s"Result ${i + 1} with score ${result.score}:")
                            result.recordings.foreach{ recording =>
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
          case Left(e) =>
            Console.err.println(s"Error creating fingerprint: ${e.getMessage}")
        }
      }
    }
}
