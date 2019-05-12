import sbt._
import Keys._
import sbtassembly.AssemblyPlugin
import AssemblyPlugin.autoImport._

object AssemblySettings extends AutoPlugin {

  override def requires: Plugins = AssemblyPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
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
    assemblyJarName in assembly := s"${name.value}-standalone-${version.value}.jar"
  )
}
