package chromaprint

import spire.math.UInt

class HammingDistanceSpec extends AbstractSpec {

  behavior of "Hamming distance"

  it should "count set bits 32" in {
    HammingDistance.countSetBits(UInt(0x00)) should equal (0)
    HammingDistance.countSetBits(UInt(0xFF)) should equal (8)
    HammingDistance.countSetBits(UInt(0xFFFF)) should equal (16)
    HammingDistance.countSetBits(UInt(0xFFFFFF)) should equal (24)
    HammingDistance.countSetBits(UInt(0xFFFFFFFF)) should equal (32)
    HammingDistance.countSetBits(UInt(0x01010101)) should equal (4)
  }
}
