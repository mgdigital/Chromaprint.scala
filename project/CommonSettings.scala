import sbt._
import Keys._

object CommonSettings extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  trait Keys {
    lazy val baseRoot = settingKey[File]("Base root")
    lazy val baseTarget = settingKey[File]("Base target")
  }

  object autoImport extends Keys

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := "2.12.8",
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    resolvers ++= Seq(
      Resolver.mavenCentral,
      Resolver.sonatypeRepo("releases")
    ),
    libraryDependencies ++= Seq(
      Dependencies.scalaTest
    ),
    sourceDirectory := baseDirectory.value,
    baseRoot := baseDirectory.value / ".." / "..",
    baseTarget := baseRoot.value / "target",
    target := baseTarget.value / "modules" / name.value,
    scalacOptions ++= Seq(
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
  )
}
