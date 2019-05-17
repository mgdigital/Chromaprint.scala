trait EnvConfigured {

  protected def envPrefix: String

  protected def envVarName(suffix: String): String =
    s"${envPrefix}_$suffix"

  protected def envVar(suffix: String): Option[String] =
    System.getenv(envVarName(suffix)) match {
      case str: String if str != "" =>
        Some(str)
      case _ =>
        None
    }
}
