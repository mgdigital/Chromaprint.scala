package chromaprint.core

import spire.math.UInt

class TestHelperSpec extends AbstractSpec {

  behavior of "TestHelper"

  it should "count set bits 32" in {
    TestHelper.countSetBits(UInt(0x00)) should equal (0)
    TestHelper.countSetBits(UInt(0xFF)) should equal (8)
    TestHelper.countSetBits(UInt(0xFFFF)) should equal (16)
    TestHelper.countSetBits(UInt(0xFFFFFF)) should equal (24)
    TestHelper.countSetBits(UInt(0xFFFFFFFF)) should equal (32)
    TestHelper.countSetBits(UInt(0x01010101)) should equal (4)
  }

}
