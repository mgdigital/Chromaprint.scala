import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.mgdigital"
ThisBuild / organizationName := "chromaprint"

lazy val root = (project in file("."))
  .settings(
    name := "chromaprint",
    resolvers ++=
      Seq(
        Resolver.mavenCentral,
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots")
      ),
    libraryDependencies ++= Seq(
      "org.scalanlp" %% "breeze" % "0.13.2",
      "org.scalanlp" %% "breeze-natives" % "0.13.2",
      "com.googlecode.soundlibs" % "mp3spi" % "1.9.5.4",
      "org.jflac" % "jflac-codec" % "1.5.2",
      "com.googlecode.soundlibs" % "tritonus-share" % "0.3.7.4",
      "com.googlecode.soundlibs" % "tritonus-all" % "0.3.7.2",
      "com.github.scopt" %% "scopt" % "4.0.0-RC2",
      "com.softwaremill.sttp" %% "core" % "1.5.15",
      "com.typesafe.play" %% "play-json" % "2.7.2",
      scalaTest % Test
    ),
    parallelExecution in Test := false,
    javaCppPresetLibs := Seq(
      "fftw" -> "3.3.7"
    ),
    javaCppVersion := "1.4.4"
  )


// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

// ThisBuild / description := "Some descripiton about your project."
// ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// ThisBuild / homepage    := Some(url("https://github.com/example/project"))
// ThisBuild / scmInfo := Some(
//   ScmInfo(
//     url("https://github.com/your-account/your-project"),
//     "scm:git@github.com:your-account/your-project.git"
//   )
// )
// ThisBuild / developers := List(
//   Developer(
//     id    = "Your identifier",
//     name  = "Your Name",
//     email = "your@email",
//     url   = url("http://your.url")
//   )
// )
// ThisBuild / pomIncludeRepository := { _ => false }
// ThisBuild / publishTo := {
//   val nexus = "https://oss.sonatype.org/"
//   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }
// ThisBuild / publishMavenStyle := true
