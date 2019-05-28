package chromaprint

class AudioSourceSpec extends AbstractSpec {

  behavior of "Audio source"

  def readAudioDuration(extension: String): Unit = {
    val source = TestHelper.audioSource(extension)
    val duration = source.duration.unsafeRunSync()
    duration should be (4.65F +- 0.1F)
  }

  def readAudioStream(extension: String): Unit = {
    val source = TestHelper.audioSource(extension)
    val audio = source.audioStream(Config.Defaults.sampleRate).take(10).compile.toVector.unsafeRunSync()
    audio should have length 10
  }

  it should "read wav duration" in {
    readAudioDuration("wav")
  }

  it should "read wav audio" in {
    readAudioStream("wav")
  }

  it should "read flac duration" in {
    readAudioDuration("flac")
  }

  it should "read flac audio" in {
    readAudioStream("flac")
  }

  it should "read mp3 duration" in {
    readAudioDuration("mp3")
  }

  it should "read mp3 audio" in {
    readAudioStream("mp3")
  }

  it should "read ogg duration" in {
    readAudioDuration("ogg")
  }

  it should "read ogg audio" in {
    readAudioStream("ogg")
  }

}
