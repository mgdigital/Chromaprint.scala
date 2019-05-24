import sbt._
import Keys._
import scoverage.ScoverageSbtPlugin
import scoverage.ScoverageKeys._

object ScoverageSettings extends AutoPlugin{

  override def requires: Plugins = ScoverageSbtPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    coverageEnabled in Test := true
  )
}
