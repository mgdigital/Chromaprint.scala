import sbt._
import Keys._

object VersionProperties extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  trait Keys {
    lazy val makeVersionProperties = taskKey[Seq[File]]("Makes a version.properties file.")
  }

  object autoImport extends Keys

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    makeVersionProperties := {
      val propFile = new File((resourceManaged in Compile).value, "version.properties")
      val content = "version=%s\n" format version.value
      IO.write(propFile, content)
      Seq(propFile)
    },
    resourceGenerators in Compile += makeVersionProperties
  )

}
