package chromaprint

import quick._

object Main extends App {

  override def main(args: Array[String]): Unit =
    cli.Command(args.toVector)

}
