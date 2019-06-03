package chromaprint.cli

import chromaprint.{AbstractSpec, TestHelper}

class ParserSpec extends AbstractSpec {

  behavior of "Command parser"

  it should "parse a command" in {
    val audioFile = TestHelper.audioFile("flac")
    val strArgs = Seq(audioFile.getAbsolutePath)
    val Right(args) = Command.args.parse(strArgs)
    args.sources should have length 1
  }
}
