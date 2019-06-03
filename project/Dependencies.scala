import sbt._

object Dependencies {

  object Versions {
    val spire = "0.16.1"
    val fs2 = "1.0.4"
    val circe = "0.11.1"
    val http4s = "0.20.1"
    val console4Cats = "0.7.0"
    val decline = "0.6.2"
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

  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val http4sClient = "org.http4s" %% "http4s-client" % Versions.http4s
  val http4sCirce = "org.http4s" %% "http4s-circe" % Versions.http4s
  val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % Versions.http4s

  val console4Cats = "dev.profunktor" %% "console4cats" % Versions.console4Cats
  val decline = "com.monovore" %% "decline" % Versions.decline

  val breezeCore = "org.scalanlp" %% "breeze" % Versions.breeze
  val breezeNatives = "org.scalanlp" %% "breeze-natives" % Versions.breeze % Optional

  val jflacCodec = "org.jflac" % "jflac-codec" % Versions.jflacCodec
  val tritonusShare = "com.googlecode.soundlibs" % "tritonus-share" % Versions.tritonusShare
  val tritonusAll = "com.googlecode.soundlibs" % "tritonus-all" % Versions.tritonusAll

  val mp3Codec = "com.googlecode.soundlibs" % "mp3spi" % Versions.mp3Codec
  val vorbisCodec = "com.googlecode.soundlibs" % "vorbisspi" % Versions.vorbisCodec

  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
}
