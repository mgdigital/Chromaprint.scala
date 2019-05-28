package chromaprint

abstract class AbstractFingerprinterSpec extends AbstractSpec {

  behavior of "Fingerprinter"

  def fftImpl: FFT

  def generateFingerprint(extension: String): Unit = {
    val source = TestHelper.audioSource(extension)
    val fingerprint = Fingerprinter(Presets.default, source)(fftImpl).unsafeRunSync()
    val expected = TestHelper.fpcalcRawData

    println(s"length:${fingerprint.length}")
//    val matchResult = FingerprintMatcher(fingerprint.data, expected)
//    println(extension + matchResult)
//    val expectedCompressed = TestHelper.fpcalcBody

//    fingerprint.data should equal (expectedRaw)
//    fingerprint.compressed should equal (expectedCompressed)

  }

  it should "Generate a wav fingerprint" in {
    generateFingerprint("wav")
  }

  it should "Generate a flac fingerprint" in {
    generateFingerprint("flac")
  }

  it should "Generate a mp3 fingerprint" in {
    generateFingerprint("mp3")
  }
}
