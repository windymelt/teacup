package teacup

final class Teacup {
  def args = Teacup.args$

  import os._

  val cmds = args
  os.proc(cmds)
    .call(
      cwd = pwd,
      stdout = Inherit,
      stderr = Inherit,
      propagateEnv = true
    )
}

object Teacup {
  private var args$opt0 = Option.empty[Array[String]]
  def args$set(args: Array[String]): Unit = {
    args$opt0 = Some(args)
  }
  def args$opt: Option[Array[String]] = args$opt0
  def args$ : Array[String] = args$opt.getOrElse {
    sys.error("No arguments passed to this script")
  }

  lazy val script = new Teacup

  def main(args: Array[String]): Unit = {
    args$set(args)
    scribe.info(s"Running script with args: ${args.mkString(", ")}")
    val _ =
      script
        .hashCode() // hashCode to clear scalac warning about pure expression in statement position
  }
}
