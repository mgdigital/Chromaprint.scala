import sbt._
import Keys._
import CommonSettings.autoImport._

object ScalaTestSettings extends AutoPlugin {

  override def requires: Plugins = CommonSettings

  override def trigger: PluginTrigger = allRequirements

  val scalaTestResultOutputPath = settingKey[File]("Test result output path")

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaTestResultOutputPath := baseTarget.value / ("scala-" + scalaBinaryVersion.value) / "test-results" / "scalatest",
    testOptions in Test ++= Seq(
      Tests.Argument(
        TestFrameworks.ScalaTest,
        "-oD"
      ),
      Tests.Argument(
        TestFrameworks.ScalaTest,
        "-u",
        scalaTestResultOutputPath.value.getAbsolutePath
      )
    ),
    parallelExecution in Test := false,
    logBuffered in Test := false
  )
}
