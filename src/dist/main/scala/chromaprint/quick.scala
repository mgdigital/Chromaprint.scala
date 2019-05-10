package chromaprint

object quick extends fingerprinter {

  implicit val fftProvider: FFT = breeze.FFTImplAsync
}
