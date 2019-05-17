import sbt._
import Keys._
import bintray.BintrayPlugin
import BintrayPlugin.autoImport._

object BintraySettings extends AutoPlugin {

  override def requires: Plugins = BintrayPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    bintrayRepository := "chromaprint",
    bintrayVcsUrl := scmInfo.value.map(_.browseUrl.toString)
  )

}
