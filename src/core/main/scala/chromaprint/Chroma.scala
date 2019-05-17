package chromaprint

import fs2.Pipe
import spire.math.UShort

import scala.collection.immutable.SortedMap
import scala.math.log

object Chroma {

  val numBands: Int = 12

  def indexToFreq(i: Int, frameSize: Int, sampleRate: Int): Double =
    i.toDouble * sampleRate / frameSize

  def freqToIndex(freq: Double, frameSize: Int, sampleRate: Int): Int =
    (frameSize * freq / sampleRate).round.toInt

  val octaveBase: Double = 440D / 16

  def freqToOctave(freq: Double): Double =
    log(freq / octaveBase) / log(2D)

  final case class Range
  (
    minFreq: Int,
    maxFreq: Int,
    frameSize: Int,
    sampleRate: Int
  ) {

    lazy val minIndex: Int =
      1 max freqToIndex(minFreq, frameSize, sampleRate)

    lazy val maxIndex: Int =
      (frameSize / 2) min freqToIndex(maxFreq, frameSize, sampleRate)

    lazy val notes: SortedMap[Int,Note] =
      (minIndex until maxIndex)
        .foldLeft(SortedMap.empty[Int,Note]) { (m, i) =>
          val freq: Double = indexToFreq(i, frameSize, sampleRate)
          val octalFreq: Double = freqToOctave(freq)
          val rawNote: Double = (octalFreq - octalFreq.floor) * numBands
          val byte: UShort = UShort(rawNote.toChar)
          val frac: Double = rawNote - byte.toDouble
          m.updated(i, Note(byte, frac))
        }

    def featureBandRegular(frame: Vector[Double], band: Int): Double =
      notes.filter(_._2.byte.toInt == band)
        .map(n => frame.lift(n._1).getOrElse(0D))
        .sum

    def featureBandInterpolated(frame: Vector[Double], band: Int): Double =
      notes.filter(_._2.byte.toInt == band)
        .map(n => frame.lift(n._1).getOrElse(0D) * n._2.interpolate.frac)
        .sum +
        notes.filter(_._2.interpolate.byte.toInt == band)
          .map(n => frame.lift(n._1).getOrElse(0D) * (1D - n._2.interpolate.frac)).sum

  }

  final case class Note(byte: UShort, frac: Double) {

    lazy val interpolate: Note =
      frac match {
        case f if f < 0.5 =>
          Note(
            (byte + UShort(numBands) - UShort(1)) % UShort(numBands),
            0.5 + frac
          )
        case f if f > 0.5 =>
          Note(
            (byte + UShort(1)) % UShort(numBands),
            1.5 - frac
          )
        case _ =>
          Note(
            byte,
            1.0
          )
      }

  }

  object Config {

    def apply(range: Range): Config =
      Config(range, interpolate = false)
  }

  final case class Config(range: Range, interpolate: Boolean)

  def pipe[F[_]](config: Config): Pipe[F,Vector[Double],Vector[Double]] = {

    import config._

    def features(frame: Vector[Double]): Vector[Double] =
      (0 until numBands).map(featureBand(frame, _)).toVector

    def featureBand(frame: Vector[Double], band: Int): Double =
      if (interpolate) {
        range.featureBandInterpolated(frame, band)
      } else {
        range.featureBandRegular(frame, band)
      }

    _.map(features)
  }

}
