package chromaprint

import spire.math.{UInt, UShort}

object FingerprintDecompressor {

  final class DecompressorException(message: String) extends Exception(message)

  def apply(data: String): Either[DecompressorException,(Int, Vector[UInt])] =
    Base64.decode(data) match {
      case Right(bytes) =>
        apply(bytes)
      case Left(e) =>
        Left(new DecompressorException("Invalid Base64 string: " + e.getMessage))
    }

  def apply(bytes: Vector[Byte]): Either[DecompressorException,(Int, Vector[UInt])] =
    if (bytes.length < 5) {
      Left(new DecompressorException("Invalid fingerprint (shorter than 5 bytes)"))
    } else {
      val ( header, body ) = bytes.splitAt(4)
      val algorithm: Int = header(0).toInt
      val length: Int = ((0xff & header(1)) << 16) | ((0xff & header(2)) << 8) | (0xff & header(3))
      if (algorithm < 0) {
        Left(new DecompressorException("Invalid algorithm"))
      } else if (length < 1) {
        Left(new DecompressorException("Invalid length"))
      } else {
        extractNormalBits(
          body,
          length
        ) match {
          case Left(e) =>
            Left(e)
          case Right(normalBits) =>
            extractExceptionBits(
              body,
              normalBits
            ) match {
              case Left(e) =>
                Left(e)
              case Right(exceptionBits) =>
                Right(
                  (
                    algorithm,
                    unpackBits(combineBits(normalBits, exceptionBits))
                  )
                )
            }
        }
      }
    }

  private def extractNormalBits
  (
    bytes: Vector[Byte],
    length: Int
  ): Either[DecompressorException,Vector[Vector[UShort]]] = {

    var result: Vector[Vector[UShort]] = Vector.empty
    var current: Vector[UShort] = Vector.empty

    def offset: Int = result.flatten.length + current.length
    def incomplete: Boolean = result.length < length

    val triplets = bytesToTriplets(bytes)

    while (incomplete && triplets.lift(offset).isDefined) {
      val bit = triplets(offset)
      current = current :+ bit
      if (bit.toInt == 0) {
        result = result :+ current
        current = Vector.empty
      }
    }

    if (incomplete) {
      Left(new DecompressorException("Not enough normal bits"))
    } else {
      Right(result)
    }
  }

  private def bytesToTriplets(bytes: Vector[Byte]): Vector[UShort] =
    bytes.grouped(3).flatMap { t =>
      Vector(
        t(0) & 0x07,
        (t(0) & 0x38) >> 3
      ) ++ (t.lift(1) match {
        case None =>
          Vector.empty[Int]
        case Some(t1) =>
          Vector(
            ((t(0) & 0xc0) >> 6) | ((t1 & 0x01) << 2),
            (t1 & 0x0e) >> 1,
            (t1 & 0x70) >> 4
          ) ++ (t.lift(2) match {
            case None =>
              Vector.empty[Int]
            case Some(t2) =>
              Vector(
                ((t1 & 0x80) >> 7) | ((t2 & 0x03) << 1),
                (t2 & 0x1c) >> 2,
                (t2 & 0xe0) >> 5
              )
          })
      })
    }.map(UShort(_)).toVector

  private def packedTripletSize(size: Int): Int =
    (size * 3 + 7) / 8

  private def extractExceptionBits
  (
    body: Vector[Byte],
    normalBits: Vector[Vector[UShort]]
  ): Either[DecompressorException,Vector[Vector[Option[UShort]]]] = {

    val quintets = bytesToQuintets(body.drop(packedTripletSize(normalBits.flatten.length)))

    val maxNormals: Map[Int,Set[Int]] = normalBits.zipWithIndex
      .map(t => (t._2, t._1.zipWithIndex.filter(_._1.toInt == 7).map(_._2).toSet))
      .foldLeft(Map.empty[Int,Set[Int]]){ (m, n) => m.updated(n._1, n._2)}

    val requiredExceptionBits = maxNormals.values.flatten.toVector.length

    if (requiredExceptionBits > quintets.length) {

      Left(new DecompressorException("Not enough exception bits"))
    } else {

      var offset: Int = 0

      Right(normalBits.indices.map{ i =>
        maxNormals(i).foldLeft(Vector.fill[Option[UShort]](normalBits(i).length)(None)){ (v, n) =>
          val b = quintets(offset)
          offset += 1
          v.updated(n, Some(b))
        }
      }.toVector)
    }
  }

  // scalastyle:off magic.number

  private def bytesToQuintets(bytes: Vector[Byte]): Vector[UShort] =
    bytes.grouped(5).flatMap{ q =>
      Vector(
        q(0) & 0x1f
      ) ++ (q.lift(1) match {
        case None =>
          Vector.empty[Int]
        case Some(q1) =>
          Vector(
            ((q(0) & 0xe0) >> 5) | ((q1 & 0x03) << 3),
            (q1 & 0x7c) >> 2
          ) ++ (q.lift(2) match {
            case None =>
              Vector.empty[Int]
            case Some(q2) =>
              Vector(
                ((q1 & 0x80) >> 7) | ((q2 & 0x0f) << 1)
              ) ++ (q.lift(3) match {
                case None =>
                  Vector.empty[Int]
                case Some(q3) =>
                  Vector(
                    ((q2 & 0xf0) >> 4) | ((q3 & 0x01) << 4),
                    (q3 & 0x3e) >> 1
                  ) ++ (q.lift(4) match {
                    case None =>
                      Vector.empty[Int]
                    case Some(q4) =>
                      Vector(
                        ((q3 & 0xc0) >> 6) | ((q4 & 0x07) << 2),
                        (q4 & 0xf8) >> 3
                      )
                  })
              })
          })
      })
    }.map(UShort(_)).toVector

  // scalastyle:on magic.number

  private def combineBits
  (
    normalBits: Vector[Vector[UShort]],
    exceptionBits: Vector[Vector[Option[UShort]]]
  ): Vector[UShort] =
    normalBits.zip(exceptionBits).flatMap{ p =>
      p._1.zipWithIndex.map{ b =>
        if (b._1.toInt == 7) {
          b._1 + p._2(b._2).getOrElse{throw new RuntimeException("Exception bit not found")}
        } else {
          b._1
        }
      }
    }

  private def unpackBits(bits: Vector[UShort]): Vector[UInt] =
    bits.foldLeft((UInt(0), UInt(0), Vector.empty[UInt])){ (t, bit) =>
      val ( value, lastBit, result ) = t
      if (bit.toInt == 0) {
        (
          UInt(0),
          UInt(0),
          result :+ result.lastOption.map(value ^ _).getOrElse(value)
        )
      } else {
        val nextLast = lastBit + UInt(bit.toInt)
        (
          value | UInt(1) << (nextLast.toInt - 1),
          nextLast,
          result
        )
      }
    }._3

}
