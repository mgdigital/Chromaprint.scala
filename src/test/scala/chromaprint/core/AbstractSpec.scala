package chromaprint.core

import org.scalatest._

abstract class AbstractSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors
