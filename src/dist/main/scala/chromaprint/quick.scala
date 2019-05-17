package chromaprint

object quick {

  implicit val fftImpl: FFT = breeze.FFTImpl

  object fingerprinter extends chromaprint.fingerprinter
}
