package chromaprint

import java.io.File
import scala.io.Source

import spire.math.UInt

object TestHelper {

  def fpcalcOutToBody(output: String): Option[String] =
    output.split('\n').find(_.startsWith("FINGERPRINT=")).map(_.drop(12).trim)

  lazy val fpcalcOut: String =
    Source.fromURL(getClass.getResource("/fpcalc.out")).mkString

  lazy val fpcalcRawOut: String =
    Source.fromURL(getClass.getResource("/fpcalc-raw.out")).mkString

  lazy val fpcalcBody: String =
    fpcalcOutToBody(fpcalcOut).get

  lazy val fpcalcRawBody: String =
    fpcalcOutToBody(fpcalcRawOut).get

  lazy val fpcalcRawData: Vector[UInt] =
    fpcalcRawBody.split(',').toVector.map(_.toLong).map(UInt(_))

  def audioSource(extension: String): AudioSource =
    AudioSource(
      new File(getClass.getResource(s"/kickinmule.$extension").getFile)
    )
}
