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
    email = "mike+sonatype@mgdigital.co.uk",
    url   = url("https://github.com/mgdigital")
  )
)

ThisBuild / description := "A Scala implementation of the Chromaprint/AcoustID audio fingerprinting algorithm."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/mgdigital/Chromaprint.scala"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}
ThisBuild / publishMavenStyle := true

val makeVersionProperties = taskKey[Seq[File]]("Makes a version.properties file.")

val baseTarget = settingKey[File]("Base target")

lazy val commonSettings = Seq(
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.11.12", "2.12.8"),
  resolvers ++=
    Seq(
      Resolver.mavenCentral,
      Resolver.sonatypeRepo("releases"),
    ),
  parallelExecution in Test := false,
  sourceDirectory := baseDirectory.value,
  baseTarget := baseDirectory.value / ".." / ".." / "target",
  target := baseTarget.value / "modules" / name.value,
  makeVersionProperties := {
    val propFile = new File((resourceManaged in Compile).value, "version.properties")
    val content = "version=%s\n" format version.value
    IO.write(propFile, content)
    Seq(propFile)
  },
  resourceGenerators in Compile += makeVersionProperties,
  testOptions in Test ++= Seq(
    Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oD"
    ),
    Tests.Argument(
      TestFrameworks.ScalaTest,
      "-u",
      baseTarget.value + "/scala-" + scalaBinaryVersion.value + "/test-results/scalatest"
    )
  ),
  coverageEnabled := true,
  wartremoverErrors in Compile ++= Warts.unsafe.filterNot(Set(Wart.Var, Wart.Throw).contains),
  wartremoverExcluded += baseDirectory.value / "test"
)

lazy val dependencies = new {
    val versions =
      new {
        val spire = "0.16.1"
        val scopt = "4.0.0-RC2"
        val sttp = "1.5.15"
        val playJson = "2.7.2"
        val jflacCodec = "1.5.2"
        val tritonusShare = "0.3.7.4"
        val tritonusAll = "0.3.7.2"
        val breeze = "0.13.2"
        val javaCppPresets = "1.4"
        val javaCpp = javaCppPresets + ".3"
        val javaCppFftw = "3.3.7"
        val scalaTest = "3.0.5"
      }

    val spire = "org.typelevel" %% "spire" % versions.spire
    val scopt = "com.github.scopt" %% "scopt" % versions.scopt
    val sttp = "com.softwaremill.sttp" %% "core" % versions.sttp
    val playJson = "com.typesafe.play" %% "play-json" % versions.playJson
    val breezeCore = "org.scalanlp" %% "breeze" % versions.breeze
    val breezeNatives = "org.scalanlp" %% "breeze-natives" % versions.breeze % Optional
    val scalaTest = "org.scalatest" %% "scalatest" % versions.scalaTest % Test

    val jflacCodec = "org.jflac" % "jflac-codec" % versions.jflacCodec % Optional
    val tritonusShare = "com.googlecode.soundlibs" % "tritonus-share" % versions.tritonusShare % Optional
    val tritonusAll = "com.googlecode.soundlibs" % "tritonus-all" % versions.tritonusAll % Optional

    val codecs = Seq(
      jflacCodec,
      tritonusShare,
      tritonusAll
    )
  }

val mapAll = "runtime->runtime;test->test;compile->compile"

lazy val core = (project in file("./src/core"))
  .settings(
    name := "chromaprint-core",
    commonSettings,
    libraryDependencies ++= Seq(
        dependencies.spire,
        dependencies.scalaTest
      )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning)

lazy val acoustid = (project in file("./src/acoustid"))
  .settings(
    name := "chromaprint-acoustid",
    commonSettings,
    libraryDependencies ++= Seq(
      dependencies.sttp,
      dependencies.playJson
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(core % mapAll)

lazy val cli = (project in file("./src/cli"))
  .settings(
    name := "chromaprint-cli",
    commonSettings,
    libraryDependencies ++= Seq(
      dependencies.scopt
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
    commonSettings,
    libraryDependencies ++= Seq(
      dependencies.breezeCore,
      dependencies.breezeNatives
    ),
    dependencyOverrides ++= Seq(
      dependencies.spire
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(core % mapAll)

lazy val fftw = (project in file("./src/fftw"))
  .settings(
    name := "chromaprint-fftw",
    commonSettings,
    javaCppPresetLibs := Seq(
      "fftw" -> "3.3.7"
    ),
    javaCppVersion := "1.4.4"
  )
  .disablePlugins(AssemblyPlugin)
  .enablePlugins(GitVersioning)
  .dependsOn(core % mapAll)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "chromaprint",
    mainClass := Some("chromaprint.Main"),
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src" / "dist"
    ),
    sourceDirectories in Test ++= Seq("acoustid", "breeze", "cli", "core")
      .map(m => baseDirectory.value / "target" / "modules" / s"chromaprint-$m"),
    libraryDependencies ++= dependencies.codecs,
    baseTarget := baseDirectory.value / "target",
    target := baseTarget.value,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") =>
        MergeStrategy.discard
      case PathList("META-INF", _*) =>
        MergeStrategy.concat
      case PathList("spire", _*) =>
        MergeStrategy.first
      case _ =>
        MergeStrategy.deduplicate
    },
    assemblyShadeRules in assembly ++= Seq(
      ShadeRule.zap("org.spire-math.**").inAll
    ),
    assemblyJarName in assembly := s"${name.value}-assembly-${version.value}.jar"
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin)
  .enablePlugins(GitVersioning)
  .aggregate(core, acoustid, cli, breeze)
  .dependsOn(core, acoustid, cli, breeze)
