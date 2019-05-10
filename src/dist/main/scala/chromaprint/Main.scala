package chromaprint

object Main extends App {

  override def main(args: Array[String]): Unit =
    cli.command(args.toVector)(breeze.FFTImplAsync)

}
