package chromaprint.cli

import chromaprint.{AudioSource, Presets}
import chromaprint.acoustid.lookup
import chromaprint.cli.Command.Args

object Parser {

  import scopt._

  private val builder: OParserBuilder[Args] = OParser.builder[Args]

  private val parser: OParser[Unit,Args] = {
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
        .action((x, a) => a.withConfig(Presets(x))),
      opt[Int]('l', "length")
        .text("maximum duration to sample for fingerprint")
        .action((x, a) => a.withConfig(_.copy(maxDuration = x))),
      opt[Int]('o', "overlap")
        .text("overlap between frames")
        .action((x, a) => a.withConfig(_.copy(overlap = x))),
      opt[Int]('r', "rate")
        .text("sample rate")
        .action((x, a) => a.withConfig(_.copy(sampleRate = x))),
      opt[Boolean]("raw")
        .text("show raw fingerprint")
        .action((x, a) => a.withParams(_.copy(showRaw = x))),
      opt[Boolean]("compressed")
        .text("show compressed fingerprint")
        .action((x, a) => a.withParams(_.copy(showCompressed = x))),
      opt[Boolean]("hash")
        .text("show hash")
        .action((x, a) => a.withParams(_.copy(showHash = x))),
      opt[Boolean]('a', "acoustid")
        .text("lookup matches from acoustid")
        .action((x, a) => a.withParams(_.copy(acoustid = if (x) Some(lookup.Config.default) else None))),
      opt[String]("acoustid-client")
        .text("acoustid client id")
        .action((x, a) => a.withParams(_.copy(
          acoustid = a.params.acoustid.orElse(Some(lookup.Config.default)).map(_.withClientId(x))
        ))),
      opt[Int]("repetitions")
          .action((x, a) => a.withParams(_.copy(repetitions = x))),
      arg[String]("<source>...")
        .unbounded()
        .action((x, a) => {
          val Right(source) = AudioSource(x)
          a.withSource(source)
        })
        .text("files to fingerprint")
    )
  }

  def apply(args: Vector[String]): Option[Args] =
    OParser.parse(parser, args, Args.default)

}
