package chromaprint

import spire.math.UInt

class FingerprintDecompressorSpec extends AbstractSpec {

  behavior of "fingerprint decompressor"

  it should "decompress 1 item 1 bit" in {
    val compressed: Vector[Byte] = Vector(0, 0, 0, 1, 1).map(_.toByte)
    val Right((algorithm, data)) = FingerprintDecompressor(Base64(compressed))

    algorithm should equal (0)
    data should equal (Vector(UInt(1)))
  }

  it should "decompress 1 item 3 bits" in {
    val compressed: Vector[Byte] = Vector(0, 0, 0, 1, 73, 0).map(_.toByte)
    val Right((algorithm, data)) = FingerprintDecompressor(Base64(compressed))

    algorithm should equal (0)
    data should equal (Vector(UInt(7)))
  }

  it should "decompress 1 item 1 bit 1 except" in {
    val compressed: Vector[Byte] = Vector(0, 0, 0, 1, 7, 0).map(_.toByte)
    val Right((algorithm, data)) = FingerprintDecompressor(Base64(compressed))

    algorithm should equal (0)
    data should equal (Vector(UInt(1 << 6)))
  }

  it should "decompress 1 item 1 bit 2 except" in {
    val compressed: Vector[Byte] = Vector(0, 0, 0, 1, 7, 2).map(_.toByte)
    val Right((algorithm, data)) = FingerprintDecompressor(Base64(compressed))

    algorithm should equal (0)
    data should equal (Vector(UInt(1 << 8)))
  }

  it should "decompress 2 items" in {
    val compressed: Vector[Byte] = Vector(0, 0, 0, 2, 65, 0).map(_.toByte)
    val Right((algorithm, data)) = FingerprintDecompressor(Base64(compressed))

    algorithm should equal (0)
    data should equal (Vector(1, 0).map(UInt(_)))
  }

  it should "decompress 2 items no change" in {
    val compressed: Vector[Byte] = Vector(0, 0, 0, 2, 1, 0).map(_.toByte)
    val Right((algorithm, data)) = FingerprintDecompressor(Base64(compressed))

    algorithm should equal (0)
    data should equal (Vector(1, 1).map(UInt(_)))
  }

  it should "fail on invalid" in {
    val compressed: Vector[Byte] = Vector(0, 255, 255, 255).map(_.toByte)
    val result = FingerprintDecompressor(Base64(compressed))

    result.isLeft should equal (true)
  }

  it should "decompress a longer fingerprint" in {
    val strCompressed = "AQAAEwkjrUmSJQpUHflR9mjSJMdZpcO_Imdw9dCO9Clu4_wQPvhCB01w6xAtXNcAp5RASgDBhDSCGGIAcwA"
    val Right((algorithm, data)) = FingerprintDecompressor(strCompressed)
    val expected: Vector[UInt] =
      Vector(
        -587455133,
        -591649759,
        -574868448,
        -576973520,
        -543396544,
        1330439488,
        1326360000,
        1326355649,
        1191625921,
        1192674515,
        1194804466,
        1195336818,
        1165981042,
        1165956451,
        1157441379,
        1157441299,
        1291679571,
        1291673457,
        1170079601
      ).map(UInt(_))

    algorithm should equal (1)
    data should equal (expected)
  }

//  it should "decompress a fingerprint from fpcalc" in {
//    fingerprintDecompressor(TestHelper.fpcalcBody).right.get.data should equal (TestHelper.fpcalcRawData)
//  }
}
