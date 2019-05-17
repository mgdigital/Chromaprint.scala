import sbt._
import bintray.BintrayPlugin
import bintray.BintrayPlugin.autoImport._

object BintraySettings extends AutoPlugin {

  override def requires: Plugins = BintrayPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    bintrayRepository := "chromaprint"
  )

}
