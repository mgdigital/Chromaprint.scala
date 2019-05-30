package chromaprint.cli

import chromaprint.{AbstractSpec, TestHelper}

class ParserSpec extends AbstractSpec {

  behavior of "Command parser"

  it should "parse a command" in {
    val audioFile = TestHelper.audioFile("flac")
    val strArgs = Seq(audioFile.getAbsolutePath)
    val Some(args) = Parser(strArgs)
    args.sources should have length 1
  }
}
