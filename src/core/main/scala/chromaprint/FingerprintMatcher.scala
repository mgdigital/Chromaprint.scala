package chromaprint

import scala.math.random
import spire.math.UInt

object FingerprintMatcher {

  object Config {

    object Defaults {
      val maxBitError: Int = 2
      val maxAlignOffset: Int = 120
      val queryStart: Int = 80
      val queryLength: Int = 120
      val queryBits: Int = 28
      val matchThreshold: Float = 10F
    }

    val default: Config = Config(
      Defaults.maxBitError,
      Defaults.maxAlignOffset,
      Defaults.queryStart,
      Defaults.queryLength,
      Defaults.queryBits,
      Defaults.matchThreshold
    )
  }

  final case class Config
  (
    maxBitError: Int,
    maxAlignOffset: Int,
    queryStart: Int,
    queryLength: Int,
    queryBits: Int,
    matchThreshold: Float
  ) {

    val queryMask: Int =
      ((1 << queryBits) - 1) << (32 - queryBits)

    def stripQuery(x: Int): Int =
      x & queryMask
  }

  object Segment {

    def apply(pos1: Int, pos2: Int, score: Double): Segment =
      new Segment(pos1, pos2, 1, score, 0D, 0D)
  }

  final case class Segment
  (
    pos1: Int,
    pos2: Int,
    duration: Int,
    score: Double,
    leftScore: Double,
    rightScore: Double
  ) {

    lazy val publicScore: Float =
      (score * 100 + 0.5).toFloat

    def merge(other: Segment): Segment = {
      require(pos1 + duration == other.pos1)
      require(pos2 + duration == other.pos2)
      val newDuration = duration + other.duration
      Segment(
        pos1,
        pos2,
        newDuration,
        (score * duration + other.score * other.duration) / newDuration,
        score,
        other.score
      )
    }
  }

  val alignBits: Int = 12
  val hashShift: Int = 32 - alignBits

  val hashMask: UInt = ((UInt(1) << alignBits) - UInt(1)) << hashShift
  val offsetMask: UInt = (UInt(1) << (hashShift - 1)) - UInt(1)
  val sourceMask: UInt = UInt(1) << (hashShift - 1)

  def alignStrip(x: UInt): UInt =
    x >> hashShift

  val randMax: Int = 32767

  sealed class FingerprintMatcherException(message: String) extends Exception(message)

  def apply
  (
    fp1: Vector[UInt],
    fp2: Vector[UInt]
  ): Either[FingerprintMatcherException,Float] =
    apply(Config.default, fp1, fp2)

  def apply
  (
    config: Config,
    fp1: Vector[UInt],
    fp2: Vector[UInt]
  ): Either[FingerprintMatcherException,Float] = {

    import config._

    if (UInt(fp1.length + 1) >= offsetMask) {
      Left(new FingerprintMatcherException("Fingerprint 1 is too long"))
    } else if (UInt(fp2.length + 1) >= offsetMask) {
      Left(new FingerprintMatcherException("Fingerprint 2 is too long"))
    } else {

      val offsets: Vector[UInt] =
        (fp1.zipWithIndex.map{
          case (el, i) =>
            (alignStrip(el) << hashShift) | (UInt(i) & offsetMask)
        } ++ fp2.zipWithIndex.map{
          case (el, i) =>
            (alignStrip(el) << hashShift) | ((UInt(i) & offsetMask) | sourceMask)
        }).sortBy(_.toLong)

      val histogram: Vector[UInt] =
        offsets.zipWithIndex.foldLeft(Vector.fill[UInt](offsets.length)(UInt(0))){ (hist, t) =>
          val (el, i) = t
          val hash: UInt = el & hashMask
          val offset1: UInt = el & offsetMask
          val source1: UInt = el & sourceMask
          if (source1 != UInt(0)) {
            // if we got hash from fp2, it means there is no hash from fp1,
            // because if there was, it would be first
            hist
          } else {
            offsets.drop(i + 1).foldLeft((hist, true)){
              (t2, el2) =>
                t2 match {
                  case (hist2, true) =>
                    val hash2: UInt = el2 & hashMask
                    if (hash != hash2) {
                      (hist2, false)
                    } else {
                      val offset2: UInt = el2 & offsetMask
                      val source2: UInt = el2 & sourceMask
                      if (source2 != UInt(0)) {
                        val offsetDiff = (offset1 + UInt(fp2.length) - offset2).toInt
                        (hist2.updated(offsetDiff, hist2(offsetDiff) + UInt(1)), true)
                      } else {
                        (hist2, true)
                      }
                    }
                  case other =>
                    other
                }
            }._1
          }
        }

      val bestAlignments: Vector[(UInt, Int)] =
        histogram.zipWithIndex.filter(_._1 > UInt(1)).foldLeft(Vector.empty[(UInt,Int)]){ (al, t) =>
          val (count, i) = t
          val isPeakLeft: Boolean =
            if (i > 0) {
              histogram(i - 1) <= count
            } else {
              true
            }
          if (isPeakLeft) {
            val isPeakRight: Boolean =
              if (i < histogram.length - 1) {
                histogram(i + 1) <= count
              } else {
                true
              }
            if (isPeakRight) {
              al :+ (count, i)
            } else {
              al
            }
          } else {
            al
          }
        }.sortBy(- _._1.toLong)

      var segment: Option[Segment] = None

      bestAlignments.headOption.foreach { al =>
        val offsetDiff: Int = al._2 - fp2.length
        var offset1: Int = if (offsetDiff > 0) offsetDiff else 0
        var offset2: Int = if (offsetDiff < 0) - offsetDiff else 0
        val size: Int = (fp1.length - offset1) min (fp2.length - offset2)
        val bitCounts: IndexedSeq[Float] = (0 until size).map{ _ =>
          val c: Float = HammingDistance(fp1(offset1), fp2(offset2)) + (random * (0.001D / randMax)).toFloat
          offset1 += 1
          offset2 += 1
          c
        }
        val smoothedBitCounts: IndexedSeq[Float] = GaussianFilter(8D, 3, bitCounts)
        val gradient: IndexedSeq[Float] = Gradient(smoothedBitCounts)
          .map(_.abs)
        var gradientPeaks: IndexedSeq[Int] = IndexedSeq.empty
        val bitCountsSum = bitCounts.sum
        (0 until size) foreach { i =>
          val score = bitCountsSum / (i + 1)
          if (score < matchThreshold) {
            val nextSegment = Segment(offset1 + i, offset2 + i, score)
            segment = segment
              .filter(s => (s.score - score).abs < 0.7F)
                .map(_.merge(nextSegment))
                .orElse(Some(nextSegment))
          }
        }
      }

      Right(segment.map(_.publicScore).getOrElse(0F))
    }
  }
}
