import sbt._
import Keys._
import wartremover.WartRemover
import WartRemover.autoImport._

object WartRemoverSettings extends AutoPlugin {

  override def requires: Plugins = WartRemover

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    wartremoverErrors in Compile ++= Warts.unsafe.filterNot(Set(
      Wart.Var,
      Wart.Throw,
      Wart.Any,
      Wart.EitherProjectionPartial,
    ).contains),
    wartremoverExcluded += baseDirectory.value / "test"
  )
}
