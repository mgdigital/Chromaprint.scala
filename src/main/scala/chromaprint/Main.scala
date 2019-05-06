package chromaprint

object Main extends App {

  override def main(args: Array[String]): Unit =
    fpcalc.Command(args.toVector)(breeze.FFT)

}
