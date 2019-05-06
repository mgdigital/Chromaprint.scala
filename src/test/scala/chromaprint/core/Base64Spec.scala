package chromaprint.core

class Base64Spec extends AbstractSpec {

  behavior of "Base64"

  def encodeString(str: String): String =
    Base64(str.toVector.map(_.toByte))

  it should "encode" in {
    encodeString("x") should equal ("eA")
    encodeString("xx") should equal ("eHg")
    encodeString("xxx") should equal ("eHh4")
    encodeString("xxxx") should equal ("eHh4eA")
    encodeString("xxxxx") should equal ("eHh4eHg")
    encodeString("xxxxxx") should equal ("eHh4eHh4")
    Base64(Vector(0xff, 0xee).map(_.toByte)) should equal ("_-4")
  }

  val data: Vector[Byte] =
    Vector(
      1, 0, 1, 207, 17, 181, 36, 18, 19, 37, 65, 15, 31, 197, 149, 161, 63, 33, 22,
      60, 141, 27, 202, 35, 184, 47, 254, 227, 135, 135, 11, 58, 139, 208, 65, 127,
      52, 167, 241, 31, 99, 182, 25, 159, 96, 70, 71, 160, 251, 168, 75, 132, 185,
      112, 230, 193, 133, 252, 42, 126, 66, 91, 121, 60, 135, 79, 24, 185, 210, 28,
      199, 133, 255, 240, 113, 101, 67, 199, 23, 225, 181, 160, 121, 140, 67, 123,
      161, 229, 184, 137, 30, 205, 135, 119, 70, 94, 252, 71, 120, 150
    ).map(_.toByte)

  val encoded: String =
    "AQABzxG1JBITJUEPH8WVoT8hFjyNG8ojuC_-44eHCzqL0EF_NKfxH2O2GZ9gRkeg-6hLhLlw5sGF_Cp-Qlt5PIdPGLnSHMeF__BxZUPHF-G1oHmMQ3uh5biJHs2Hd0Ze_Ed4lg"

  it should "encode long" in {
    Base64(data) should equal (encoded)
  }

  it should "decode long" in {
    Base64.unapply(encoded).get should equal (data)
  }
}
