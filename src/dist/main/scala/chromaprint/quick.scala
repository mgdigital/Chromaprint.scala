package chromaprint

object quick extends fingerprinter {

  implicit val fftImpl: FFT = breeze.FFTImplAsync
}
