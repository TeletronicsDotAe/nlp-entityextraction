package ae.teletronics.nlp.entityextraction.stanford

import org.junit.Test
import org.junit.Assert.assertEquals

/**
  * Created by trym on 21-06-2016.
  */
class StanfordNLPEngineTest {
  private val underTest = new StanfordNLPEngine

  @Test
  def testFirstLastNameAsOne() = {
    val keanu = "Keanu Charles Reeves"
    val matrixSummary = s"Neo (${keanu}) believes that Morpheus (Laurence Fishburne), " +
      s"an elusive figure considered to be the most dangerous man alive, can answer his question -- " +
      s"What is the Matrix? Neo is contacted by Trinity (Carrie-Anne Moss), " +
      s"a beautiful stranger who leads him into an underworld where he meets Morpheus."

    val entities = underTest.process(matrixSummary)
    assertEquals(keanu, entities.persons.head)
  }

}
