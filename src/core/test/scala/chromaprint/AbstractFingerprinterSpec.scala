package chromaprint

abstract class AbstractFingerprinterSpec extends AbstractSpec {

  behavior of "Fingerprinter"

  def fftImpl: FFT

  it should "Generate a fingerprint" in {
    val source = TestHelper.audioSource("wav")
    val fingerprint = Fingerprinter(source)(fftImpl).unsafeRunSync()
    val expectedRaw = TestHelper.fpcalcRawData
    val expectedCompressed = TestHelper.fpcalcBody

    fingerprint.data should equal (expectedRaw)
    fingerprint.compressed should equal (expectedCompressed)
  }
}
