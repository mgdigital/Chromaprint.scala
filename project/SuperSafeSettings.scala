import sbt._
import Keys._
import com.artima.SuperSafeSbtPlugin

object SuperSafeSettings extends AutoPlugin {

  override def requires: Plugins = SuperSafeSbtPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
  )
}
