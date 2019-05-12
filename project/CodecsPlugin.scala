import sbt._
import Keys._

object CodecsPlugin extends AutoPlugin {

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      Dependencies.jflacCodec,
      Dependencies.tritonusShare,
      Dependencies.tritonusAll
    )
  )
}
