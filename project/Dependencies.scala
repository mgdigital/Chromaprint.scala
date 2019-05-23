import sbt._

object Dependencies {

  object Versions {
    val spire = "0.16.1"
    val fs2 = "1.0.4"
    val scopt = "4.0.0-RC2"
    val sttp = "1.5.15"
    val playJson = "2.7.2"
    val jflacCodec = "1.5.2"
    val mp3Codec = "1.9.5-1"
    val vorbisCodec = "1.0.3-1"
    val tritonusShare = "0.3.7.4"
    val tritonusAll = "0.3.7.2"
    val breeze = "0.13.2"
    val javaCppPresets = "1.4"
    val javaCpp: String = javaCppPresets + ".3"
    val javaCppFftw = "3.3.7"
    val scalaTest = "3.0.5"
  }

  val spire = "org.typelevel" %% "spire" % Versions.spire
  val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2
  val scopt = "com.github.scopt" %% "scopt" % Versions.scopt
  val sttp = "com.softwaremill.sttp" %% "core" % Versions.sttp
  val playJson = "com.typesafe.play" %% "play-json" % Versions.playJson
  val breezeCore = "org.scalanlp" %% "breeze" % Versions.breeze
  val breezeNatives = "org.scalanlp" %% "breeze-natives" % Versions.breeze % Optional
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % Test

  val jflacCodec = "org.jflac" % "jflac-codec" % Versions.jflacCodec
  val tritonusShare = "com.googlecode.soundlibs" % "tritonus-share" % Versions.tritonusShare
  val tritonusAll = "com.googlecode.soundlibs" % "tritonus-all" % Versions.tritonusAll

  val mp3Codec = "com.googlecode.soundlibs" % "mp3spi" % Versions.mp3Codec
  val vorbisCodec = "com.googlecode.soundlibs" % "vorbisspi" % Versions.vorbisCodec

}
