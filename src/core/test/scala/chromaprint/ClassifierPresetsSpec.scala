package chromaprint

class ClassifierPresetsSpec extends AbstractSpec {

  behavior of "Classifier presets"

  it should "load classifier presets" in {
    ClassifierPresets.preset0.isInstanceOf[Classifier.Config]
    ClassifierPresets.preset1.isInstanceOf[Classifier.Config]
    ClassifierPresets.preset2.isInstanceOf[Classifier.Config]
  }

}
