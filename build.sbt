import CommonSettings.autoImport._

ThisBuild / organization := "com.github.mgdigital"
ThisBuild / organizationName := "MGDigital"
ThisBuild / organizationHomepage := Some(url("https://github.com/mgdigital"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/mgdigital/Chromaprint.scala"),
    "scm:git@github.com:mgdigital/Chromaprint.scala.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "MGDigital",
    name  = "Mike Gibson",
    email = "chromaprint@mgdigital.co.uk",
    url   = url("https://github.com/mgdigital")
  )
)

ThisBuild / description := "A Scala implementation of the Chromaprint/AcoustID audio fingerprinting algorithm."
ThisBuild / licenses := List("Apache-2.0" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/mgdigital/Chromaprint.scala"))

val mapAll = "runtime->runtime;test->test;compile->compile"

lazy val core = (project in file("./src/core"))
  .settings(
    name := "chromaprint-core",
    libraryDependencies ++= Seq(
        Dependencies.fs2,
        Dependencies.spire
      )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning, CodecsPlugin)

lazy val acoustid = (project in file("./src/acoustid"))
  .settings(
    name := "chromaprint-acoustid",
    libraryDependencies ++= Seq(
      Dependencies.sttp,
      Dependencies.playJson
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(core % mapAll)

lazy val cli = (project in file("./src/cli"))
  .settings(
    name := "chromaprint-cli",
    libraryDependencies ++= Seq(
      Dependencies.scopt
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(
    core % mapAll,
    acoustid % mapAll
  )

lazy val breeze = (project in file("./src/breeze"))
  .settings(
    name := "chromaprint-breeze",
    libraryDependencies ++= Seq(
      Dependencies.breezeCore,
      Dependencies.breezeNatives
    ),
    dependencyOverrides ++= Seq(
      Dependencies.spire
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(core % mapAll)

lazy val fftw = (project in file("./src/fftw"))
  .settings(
    name := "chromaprint-fftw",
    javaCppPresetLibs := Seq(
      "fftw" -> Dependencies.Versions.javaCppFftw
    ),
    javaCppVersion := Dependencies.Versions.javaCpp
  )
  .disablePlugins(AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(core % mapAll)

lazy val root = (project in file("."))
  .settings(
    name := "chromaprint",
    mainClass := Some("chromaprint.Main"),
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src" / "dist"
    ),
    sourceDirectories in Test ++= Seq("acoustid", "breeze", "cli", "core")
      .map(m => baseDirectory.value / "target" / "modules" / s"chromaprint-$m"),
    baseRoot := baseDirectory.value,
    target := baseTarget.value
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin)
  .enablePlugins(GitVersioning, CodecsPlugin)
  .aggregate(core, acoustid, cli, breeze)
  .dependsOn(core, acoustid, cli, breeze)
