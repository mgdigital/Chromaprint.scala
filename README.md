# Chromaprint.scala

An implementation of the [Chromaprint](https://github.com/acoustid/chromaprint)/[AcoustID](https://acoustid.org/) audio fingerprinting algorithm for the JVM, created originally in C++ by [Lukáš Lalinský](https://oxygene.sk/).

[ ![Download](https://api.bintray.com/packages/mgdigital/chromaprint/chromaprint/images/download.svg) ](https://bintray.com/mgdigital/chromaprint/chromaprint/_latestVersion) [![CircleCI](https://circleci.com/gh/mgdigital/Chromaprint.scala/tree/master.svg?style=svg)](https://circleci.com/gh/mgdigital/Chromaprint.scala/tree/master) [![codecov](https://codecov.io/gh/mgdigital/Chromaprint.scala/branch/master/graph/badge.svg)](https://codecov.io/gh/mgdigital/Chromaprint.scala)

## What does it do?

It creates an [audio fingerprint](https://en.wikipedia.org/wiki/Acoustic_fingerprint) designed to identify near-identical audio tracks in the [AcoustID](https://acoustid.org/) and [MusicBrainz](https://musicbrainz.org/) databases. This can be used to identify unknown tracks, find duplicates and look up metadata.

## How does it work?

The algorithm performs a pipeline of operations on an audio stream to extract its fingerprint:

- The audio is resampled to mono at 11025Hz
- It is split into a series of short _frames_
- A [Fast Fourier Transform](https://en.wikipedia.org/wiki/Fast_Fourier_transform) is performed on each frame to extract components of different frequencies
- Audio features such as notes are extracted from each frame
- An image is created from the series of features
- A fingerprint is extracted from the image as a long series of numbers
- This raw fingerprint is compressed into a shorter string
- The string can be compared with other fingerprints to obtain a similarity score

## Installation and usage

### Requirements

This package is build for Scala 2.11 and 2.12. The [FFmpeg](https://ffmpeg.org/) library must be installed, along with codecs for files to be analyzed, the `libavcodec-extra` package on Debian should provide all of these.

### [SBT](https://www.scala-sbt.org/download.html)

In your `build.sbt`, add:

```scala
resolvers += Resolver.jcenterRepo

libraryDependencies += "com.github.mgdigital" %% "chromaprint" % "0.3.1"
```

Then in your Scala application:

```scala
import chromaprint.quick._

val file = new java.io.File("/audio/Pocket Calculator.flac")
val fingerprint = Fingerprinter(file).unsafeRunSync()

println(fingerprint.compressed)
// AQADtNQYhYkYnGhw7X...

```
Note: The fingerprint is generated using a [FS2 stream](https://github.com/functional-streams-for-scala/fs2), and the `Fingerprinter` returns a type of `IO[Fingerprint]`, which is a `Fingerprint` type wrapped in a [Cats Effect IO](https://typelevel.org/cats-effect/datatypes/io.html) type. Running `ùnsafeRunSync()` on the `IO[Fingerprint]` object is the quickest way to have it return the `Fingerprint`, a different `IO` method may be better suited to your application.

### Java interop

In your `pom.xml`, add:

```xml

    <repositories>
        <repository>
            <id>jcenter</id>
            <name>jcenter</name>
            <url>https://jcenter.bintray.com/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.github.mgdigital</groupId>
            <artifactId>chromaprint_2.12</artifactId>
            <version>0.3.1</version>
        </dependency>
    </dependencies>
```

Then in your Java application:

```java

import java.io.File;
import chromaprint.AudioSource;
import chromaprint.Fingerprint;
import chromaprint.quick.Fingerprinter;

File file = new File("/audio/Pocket Calculator.flac");
AudioSource source = new AudioSource.AudioFileSource(file);
Fingerprinter fingerprinter = new Fingerprinter();
Fingerprint fingerprint = fingerprinter.apply(source).unsafeRunSync();
System.out.println(fingerprint.compressed());
// AQADtNQYhYkYnGhw7X...
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

Get an [AcoustID](https://acoustid.org/) client ID and provide the `acoustid-client` option in the command line app to lookup matches in the AcoustID database:

```bash
$ sbt run "/audio/Pocket Calculator.flac" --acoustid-client=MyClientId
[info] Running chromaprint.Main /audio/Pocket Calculator.flac --acoustid-client=MyClientId
Fingerprinting...
Created fingerprint in 1.583s
Duration: 296.80008
Fingerprint (compressed): AQADtNQYhYkYnGhw7X....
AcoustID response received
c0c5a16f-64e0-4571-aa29-ba80e6cb7874: Result 1 with score 0.923195:
867cf1a1-c46a-4ad5-916f-0c5397ff65e5: 'Pocket Calculator' by 'Kraftwerk'
...
```
See the code for the command line app under `chromaprint.cli` for further examples.

Note that the fingerprinter requires an implicit for the FFT (Fast Fourier Transform) implementation. Adapters are provided for [Breeze](https://github.com/scalanlp/breeze) and through [FFTW](https://github.com/bytedeco/javacpp-presets/tree/master/fftw) via [JavaCPP Presets](https://github.com/bytedeco/javacpp-presets/tree/master/fftw). I have seen intermittent errors in the FFTW adapter so would recommend using Breeze.

## Performance

Performance was never going to be as good as the C++ library, which can calculate a fingerprint almost instantly - but it does need to be fast enough to be conveniently usable. On my laptop I can generate a first fingerprint in around 1.6 seconds from a standing start, with subsequent fingerprints generated in around 0.5 seconds (assuming audio files which need transcoding to PCM and resampling).

---

Copyright (c) 2019 Mike Gibson, https://github.com/mgdigital. Original Chromaprint algorithm Copyright (c) Lukáš Lalinský.

---

For Chris.
