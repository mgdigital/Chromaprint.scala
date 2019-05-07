# Chromaprint.scala

An implementation of the [Chromaprint][1]/[AcoustID][2] audio fingerprinting algorithm for the JVM, created originally in C++ by [Lukáš Lalinský][3].

## What does it do?

It creates an [audio fingerprint][4] designed to identify near-identical audio tracks in the [AcoustID][2] and [MusicBrainz][5] databases. This can be used to identify unknown tracks, find duplicates and look up metadata.

## How does it work?

The algorithm performs a pipeline of operations on an audio stream to extract its fingerprint:

- The audio is resampled to mono at 11025Hz
- It is split into a series of short _frames_
- A [Fast Fourier Transform][6] is performed on each frame to extract components of different frequencies
- Audio features such as notes are extracted from each frame
- An image is created from the series of features
- A fingerprint is extracted from the image as a long series of numbers
- This raw fingerprint is compressed into a shorter string
- The string can be compared with other fingerprints to obtain a similarity score

## Installation and usage

### Requirements

The [FFmpeg][7] library must be installed, along with codecs for files to be analyzed, the `libavcodec-extra` on Debian should provide all of these.

### Build and run with SBT

Install [SBT][8] and run the command line app:

```bash
$ sbt run "/path/to/myaudiofile.mp3"
[info] Running chromaprint.Main /path/to/myaudiofile.mp3
Fingerprinting...
Created fingerprint in 4.177s
Duration: 296.80008
Fingerprint (raw): 829534504,829526824,829526808,...
Fingerprint (compressed): AQADtNQYhYkYnGhw7X....
Hash: 813035563
```

Build an executable JAR:

```bash
$ sbt assembly
```

### Build the Docker image

```bash
$ docker build -t chromaprint-scala .
```

Then run the command line app (assuming audio file in current folder, double quotes needed for filenames containing spaces).

```bash
$ docker run -ti -v $(pwd):/audio/ chromaprint-scala '"/audio/Pocket Calculator.flac"'
```

## Identify track with AcoustID

Get an [AcoustID][2] client ID and provide the `acoustid-client` option in the command line app to lookup matches in the AcoustID database:

```bash
$ sbt run "/audio/Pocket Calculator.flac" --acoustid-client=MyClientId
[info] Running chromaprint.Main /audio/Pocket Calculator.flac --acoustid-client=MyClientId
Fingerprinting...
Created fingerprint in 4.177s
Duration: 296.80008
Fingerprint (raw): 829534504,829526824,829526808,...
Fingerprint (compressed): AQADtNQYhYkYnGhw7X....
Hash: 813035563
AcoustID response received
Result 1 with score 0.923195:
867cf1a1-c46a-4ad5-916f-0c5397ff65e5 'Pocket Calculator' by 'Kraftwerk'
```

## Using the library in a Scala application

(SBT repository details to be published shortly)

```scala
import chromaprint.core.{AudioSource,fingerprinter}
import java.io.File

val file = new File("/audio/Pocket Calculator.flac")
val Right(fingerprint) = fingerprinter(file)(fftProvider = chromaprint.breeze.FFT)

println(fingerprint.compressed)
// AQADtNQYhYkYnGhw7X...

```
See the code for the command line app under `chromaprint.fpcalc` for further examples.

Note that the fingerprinter requires an implicit for the FFT (Fast Fourier Transform) implementation. Adapters are provided for [Breeze][9] and through [FFTW][10] via [JavaCPP Presets][11]. I have seen intermittent errors in the FFTW adapter so would recommend using Breeze.

---

Copyright (c) 2019 Mike Gibson, https://github.com/mgdigital. Original Chromaprint algorithm Copyright (c) Lukáš Lalinský.

---

For Chris.



[1]: https://github.com/acoustid/chromaprint
[2]: https://acoustid.org/
[3]: https://oxygene.sk/
[4]: https://en.wikipedia.org/wiki/Acoustic_fingerprint
[5]: https://musicbrainz.org/
[6]: https://en.wikipedia.org/wiki/Fast_Fourier_transform
[7]: https://ffmpeg.org/
[8]: https://www.scala-sbt.org/download.html
[9]: https://github.com/scalanlp/breeze
[10]: http://www.fftw.org/
[11]: https://github.com/bytedeco/javacpp-presets/tree/master/fftw
