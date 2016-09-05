package ae.teletronics.nlp.entityextraction.gate

/**
  * Created by Boris on 2016-06-08.
  */
object ArabicSmsGenerator {
  def main(args: Array[String]): Unit = {
    val testcases = TestCaseReader.readANERCorpTestCases.map(_.sentence)
    for (tc <- testcases.take(400)) {
      println(s"<sms><to>9715000</to><from>9715001</from><content>${tc}</content><timestamp>1464779470433</timestamp></sms>")
    }
  }
}
