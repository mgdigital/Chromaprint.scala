package chromaprint

package object quick {

  implicit val fftImpl: FFT = breeze.FFTImpl

  object Fingerprinter extends chromaprint.Fingerprinter
}
