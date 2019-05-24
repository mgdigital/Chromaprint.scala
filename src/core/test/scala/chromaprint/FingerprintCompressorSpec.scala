package chromaprint

import spire.math.UInt

class FingerprintCompressorSpec extends AbstractSpec {

  behavior of "Fingerprint compressor"

  it should "compress 1 item 1 bit" in {
    val compressed = FingerprintCompressor(1, Vector(UInt(1)))
    val expected = Vector(0, 0, 0, 1, 1).map(_.toByte)
    val Right(actual) = Base64.decode(compressed)

    actual should equal (expected)
  }

  it should "compress 1 item 3 bits" in {
    val compressed = FingerprintCompressor(1, Vector(UInt(1 << 6)))
    val expected = Vector(0, 0, 0, 1, 7, 0).map(_.toByte)
    val Right(actual) = Base64.decode(compressed)

    actual should equal (expected)
  }

  it should "compress 1 item 1 bit except 2" in {
    val compressed = FingerprintCompressor(1, Vector(UInt(1 << 8)))
    val expected = Vector(0, 0, 0, 1, 7, 2).map(_.toByte)
    val Right(actual) = Base64.decode(compressed)

    actual should equal (expected)
  }

  it should "compress 2 items" in {
    val compressed = FingerprintCompressor(1, Vector(UInt(1), UInt(0)))
    val expected = Vector(0, 0, 0, 2, 65, 0).map(_.toByte)
    val Right(actual) = Base64.decode(compressed)

    actual should equal (expected)
  }

  it should "compress 2 items no change" in {
    val compressed = FingerprintCompressor(1, Vector(UInt(1), UInt(1)))
    val expected = Vector(0, 0, 0, 2, 1, 0).map(_.toByte)
    val Right(actual) = Base64.decode(compressed)

    actual should equal (expected)
  }

  it should "compress a fingerprint from fpcalc" in {
    FingerprintCompressor(2, TestHelper.fpcalcRawData) should equal (TestHelper.fpcalcBody)
  }

  import FingerprintCompressor.BitStringWriter

  it should "write 1 byte" in {
    val writer = BitStringWriter()
      .write(0, 2)
      .write(1, 2)
      .write(2, 2)
      .write(3, 2)
      .flushed

    writer.bytes should equal (Vector( -28 ).map(_.toByte))
  }

  it should "write 2 bytes incomplete" in {
    val writer = BitStringWriter()
      .write(0, 2)
      .write(1, 2)
      .write(2, 2)
      .write(3, 2)
      .write(1, 2)
      .flushed

    writer.bytes should equal (Vector( -28, 1 ).map(_.toByte))
  }

  it should "write 2 bytes split" in {
    val writer = BitStringWriter()
      .write(0, 3)
      .write(1, 3)
      .write(2, 3)
      .write(3, 3)
      .flushed

    writer.bytes should equal (Vector( -120, 6 ).map(_.toByte))
  }
}
