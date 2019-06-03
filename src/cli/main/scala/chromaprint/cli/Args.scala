package chromaprint.cli

import chromaprint.{AudioSource, Config}
import chromaprint.acoustid.{Config => AcoustIDConfig}

final case class Args
(
  params: Args.Params,
  sources: List[AudioSource]
)

object Args {

  final case class Params
  (
    config: Config,
    showCompressed: Boolean,
    showRaw: Boolean,
    showHash: Boolean,
    acoustId: Option[AcoustIDConfig],
    repetitions: Int
  )

}
