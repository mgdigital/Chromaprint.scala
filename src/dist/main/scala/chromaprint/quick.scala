package chromaprint

object quick {

  implicit val fftImpl: FFT = breeze.FFTImplAsync

  object fingerprinter extends chromaprint.fingerprinter
}
