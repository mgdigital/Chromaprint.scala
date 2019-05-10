lazy val commonSettings = Seq(
    version := "0.1.0-SNAPSHOT",
    organization := "com.mgdigital",
    organizationName := "chromaprint",
    scalaVersion := "2.12.8",
    crossScalaVersions := Seq("2.8.2", "2.9.3", "2.10.7", "2.11.12"),
    resolvers ++=
      Seq(
        Resolver.mavenCentral,
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots")
      ),
    parallelExecution in Test := false,
    sourceDirectory := baseDirectory.value,
    target := baseDirectory.value / ".." / ".." / "target" / "modules" / name.value,
    artifactName := { (_, module, artifact) =>
      organizationName.value + "-" + artifact.name + "-" + module.revision + "." + artifact.extension
    },
    wartremoverErrors in Compile ++= Warts.unsafe.filterNot(Seq(Wart.Var, Wart.Throw).contains(_)),
    wartremoverExcluded += baseDirectory.value / "test"
)

lazy val dependencies = new {
    val versions =
      new {
        val spire = "0.13.0"
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

    val spire = "org.spire-math" %% "spire" % versions.spire
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

lazy val mapAll = "runtime->runtime;test->test;compile->compile"

lazy val core = (project in file("./src/core"))
  .settings(
    name := "core",
    commonSettings,
    libraryDependencies ++= Seq(
      dependencies.spire,
      dependencies.scalaTest
    )
  ).disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)

lazy val acoustid = (project in file("./src/acoustid"))
  .settings(
    name := "acoustid",
    commonSettings,
    libraryDependencies ++= Seq(
      dependencies.sttp,
      dependencies.playJson
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .dependsOn(core % mapAll)

lazy val cli = (project in file("./src/cli"))
  .settings(
    name := "cli",
    commonSettings,
    libraryDependencies ++= Seq(
      dependencies.scopt
    )
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin, AssemblyPlugin)
  .dependsOn(
    core % mapAll,
    acoustid % mapAll
  )

lazy val breeze = (project in file("./src/breeze"))
  .settings(
    name := "breeze",
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
  .dependsOn(core % mapAll)

lazy val fftw = (project in file("./src/fftw"))
  .settings(
    name := "fftw",
    commonSettings,
    javaCppPresetLibs := Seq(
      "fftw" -> "3.3.7"
    ),
    javaCppVersion := "1.4.4"
  )
  .disablePlugins(AssemblyPlugin)
  .dependsOn(core % mapAll)

lazy val dist = (project in file("./src/dist"))
  .settings(
    name := "dist",
    commonSettings,
    libraryDependencies ++= dependencies.codecs,
    target := baseDirectory.value / ".." / ".." / "target",
    unmanagedSourceDirectories in Compile ++= Seq("core", "acoustid", "cli", "breeze")
      .map(baseDirectory.value / ".." / _ / "main" / "scala"),
    artifactName := { (_, module, artifact) =>
      organizationName.value + "-" + module.revision + "." + artifact.extension
    },
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") =>
        MergeStrategy.discard
      case PathList("META-INF", _*) =>
        MergeStrategy.concat
      case _ =>
        MergeStrategy.deduplicate
    },
    assemblyJarName in assembly := s"${organizationName.value}-assembly-${version.value}.jar"
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin)
  .aggregate(core, acoustid, cli, breeze)
  .dependsOn(
    core % mapAll,
    acoustid % mapAll,
    cli % mapAll,
    breeze % mapAll
  )

lazy val root = (project in file("."))
  .settings(
    name := "root",
    commonSettings,
    libraryDependencies ++= dependencies.codecs,
    unmanagedSourceDirectories in Compile ++= Seq("core", "acoustid", "cli", "breeze", "dist", "fftw")
      .map(baseDirectory.value / "src" / _ / "main" / "scala"),
    target := baseDirectory.value / "target" / name.value,
    mainClass in (Compile, run) := Some("chromaprint.Main")
  )
  .disablePlugins(org.bytedeco.sbt.javacpp.Plugin)
  .aggregate(core, acoustid, cli, breeze, fftw, dist)
  .dependsOn(
    core % mapAll,
    acoustid % mapAll,
    cli % mapAll,
    breeze % mapAll,
    fftw % mapAll,
    dist % mapAll
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
