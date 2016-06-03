package ae.teletronics.nlp.entityextraction.types.sender

import org.junit.Test

/**
  * Created by trym on 19-05-2016.
  */
class EngLearnerTest {

  @Test
  def testLearn() = {
    val underTest = new EngLearner()
    val s = this.getClass.getClassLoader.getResourceAsStream("train/mailinglists.xml")
    underTest.learn(s)
  }

  @Test
  def testSliding() = {
    val terms = List("Hej", "Trym", "hvordan", "går", "det", "i", "dag?", "Mvh.", "Thomas")
    val termWithPrevious = terms.iterator.sliding(2)
    termWithPrevious.foreach(println)
  }
}
