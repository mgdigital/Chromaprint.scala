package chromaprint.quick

object Fingerprinter extends Fingerprinter

class Fingerprinter extends chromaprint.Fingerprinter.Impl(chromaprint.breeze.FFTImpl)
