import sbt._

object Dependencies {

  object Versions {
    val spire = "0.16.1"
    val scopt = "4.0.0-RC2"
    val sttp = "1.5.15"
    val playJson = "2.7.2"
    val jflacCodec = "1.5.2"
    val tritonusShare = "0.3.7.4"
    val tritonusAll = "0.3.7.2"
    val breeze = "0.13.2"
    val javaCppPresets = "1.4"
    val javaCpp: String = javaCppPresets + ".3"
    val javaCppFftw = "3.3.7"
    val scalaTest = "3.0.5"
    val scalactic: String = scalaTest
  }

  val spire = "org.typelevel" %% "spire" % Versions.spire
  val scopt = "com.github.scopt" %% "scopt" % Versions.scopt
  val sttp = "com.softwaremill.sttp" %% "core" % Versions.sttp
  val playJson = "com.typesafe.play" %% "play-json" % Versions.playJson
  val breezeCore = "org.scalanlp" %% "breeze" % Versions.breeze
  val breezeNatives = "org.scalanlp" %% "breeze-natives" % Versions.breeze % Optional
  val scalactic = "org.scalactic" %% "scalactic" % Versions.scalactic
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % Test

  val jflacCodec = "org.jflac" % "jflac-codec" % Versions.jflacCodec % Optional
  val tritonusShare = "com.googlecode.soundlibs" % "tritonus-share" % Versions.tritonusShare % Optional
  val tritonusAll = "com.googlecode.soundlibs" % "tritonus-all" % Versions.tritonusAll % Optional

}
