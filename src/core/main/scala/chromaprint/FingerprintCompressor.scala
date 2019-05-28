package chromaprint

import spire.math.UInt

object FingerprintCompressor {

  val maxNormalValue: Int = 7
  val normalBits: Int = 3
  val exceptionBits: Int = 5

  def apply(algorithm: Int, data: IndexedSeq[UInt]): String =
    Base64(toBytes(algorithm, data))

  def toBytes(algorithm: Int, data: IndexedSeq[UInt]): IndexedSeq[Byte] = {
    var bits: Vector[Byte] = Vector.empty[Byte]
    if (data.nonEmpty) {
      bits ++= (1 until data.length).
          foldLeft(processSubFingerprint(data(0))){
            (bits, i) =>
              bits ++ processSubFingerprint(data(i) ^ data(i - 1))
          }
    }
    initBytes(algorithm, data.length) ++
      processNormal(bits) ++
      processException(bits)
  }

  def processSubFingerprint(x: UInt): IndexedSeq[Byte] = {
    var bit: Int = 1
    var lastBit: Int = 0
    var c: UInt = x
    var r: Vector[Byte] = Vector.empty
    while (c != UInt(0)) {
      if ((c & UInt(1)) != UInt(0)) {
        r :+= (bit - lastBit).toByte
        lastBit = bit
      }
      c >>= 1
      bit += 1
    }
    r :+ 0.toByte
  }

  def initBytes(algorithm: Int, length: Int): IndexedSeq[Byte] =
    Vector(
      (algorithm - 1) & 255,
      (length >> 16) & 255,
      (length >> 8) & 255,
      length & 255
    ).map(_.toByte)

  def processNormal(bits: IndexedSeq[Byte]): IndexedSeq[Byte] =
    bits.foldLeft(BitStringWriter()){
      (w, bit) => w.write(bit.toInt min maxNormalValue, normalBits)
    }.flushedBytes

  def processException(bits: IndexedSeq[Byte]): IndexedSeq[Byte] =
    bits.map(_ - maxNormalValue).
      filter(_ >= 0).
      foldLeft(BitStringWriter()){
        (w, bit) => w.write(bit, exceptionBits)
      }.flushedBytes

  object BitStringWriter {

    def apply(): BitStringWriter =
      new BitStringWriter(Vector.empty, 0, 0)
  }

  final class BitStringWriter private
  (
    val bytes: IndexedSeq[Byte],
    buffer: Int,
    bufferSize: Int
  ) {

    def flushedBytes: IndexedSeq[Byte] =
      flushed.bytes

    override def toString: String =
      Base64(flushedBytes)

    def write(x: Int, bits: Int): BitStringWriter = {
      var buffer: Int = this.buffer | (x << this.bufferSize)
      var bufferSize: Int = this.bufferSize + bits
      var value: IndexedSeq[Byte] = this.bytes
      while (bufferSize >= 8) {
        value :+= (buffer & 255).toByte
        buffer >>= 8
        bufferSize -= 8
      }
      new BitStringWriter(value, buffer, bufferSize)
    }

    def flushed: BitStringWriter = {
      var buffer: Int = this.buffer
      var bufferSize: Int = this.bufferSize
      var value: IndexedSeq[Byte] = this.bytes
      while (bufferSize > 0) {
        value :+= (buffer & 255).toByte
        buffer >>= 8
        bufferSize -= 8
      }
      new BitStringWriter(value, 0, 0)
    }
  }

}
