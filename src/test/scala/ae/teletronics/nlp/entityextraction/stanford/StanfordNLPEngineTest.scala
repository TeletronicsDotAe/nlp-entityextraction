package ae.teletronics.nlp.entityextraction.stanford

import ae.teletronics.nlp.entityextraction.Person
import ae.teletronics.nlp.entityextraction.exclusion.{FlatFileExcludeListPersister, English}
import org.hamcrest.Matchers._
import org.junit.{Ignore, Test, Assert}
import org.junit.Assert._
import scala.collection.JavaConverters._

/**
  * Created by trym on 21-06-2016.
  */
class StanfordNLPEngineTest {
  private val underTest = new StanfordNLPEngine

  @Test
  def testOneWordSentence() = {
    val text = "Hello"
    val entities = underTest.recognize(text)
    assertEquals(0, entities.persons.size)
    assertEquals(0, entities.organisations.size)
    assertEquals(0, entities.locations.size)
  }

  @Test
  def testThreeWordSentence() = {
    val text = "Hello Keanu Reeves"
    val entities = underTest.recognize(text)
    assertEquals(1, entities.persons.size)
    assertEquals(0, entities.organisations.size)
    assertEquals(0, entities.locations.size)
  }

  @Test
  def testFirstLastNameAsOne() = {
    val keanu = "Keanu Charles Reeves"
    val matrixSummary = s"Neo (${keanu}) believes that Morpheus (Laurence Fishburne), " +
      s"an elusive figure considered to be the most dangerous man alive, can answer his question -- " +
      s"What is the Matrix? Neo is contacted by Trinity (Carrie-Anne Moss), " +
      s"a beautiful stranger who leads him into an underworld where he meets Morpheus."

    val entities = underTest.recognize(matrixSummary)

    Assert.assertThat(entities.persons.asJava, hasItem(keanu))
  }

  @Test
  def testFileExcludeListPersisterExclusion() = {
    val text = "Hello Keanu Reeves"

    // this would normally be detected as a person in the text, as per the testCorrectEntityExtraction unit test above
    val keanu = "Keanu Reeves"

    val preSubj = underTest
    val preResult = preSubj.recognize(text)
    assertThat(preResult.persons.length, is(1))
    assertThat(preResult.persons.asJava, containsInAnyOrder(keanu))

    val lang = English
    val entityType = Person

    val excluder = new FlatFileExcludeListPersister(lang)
    excluder.setExcludeSet(entityType, Set(keanu))

    val postSubj = new StanfordNLPEngine(excluder)
    val postResult = postSubj.recognize(text)

    assertThat(postResult.persons.size, is(0))

    excluder.deleteExclusion(entityType, keanu);
    val postResult2 = postSubj.recognize(text)

    import java.nio.file.{Files, Paths}
    Files.deleteIfExists(Paths.get(excluder.mkFilename(entityType)))

    assertThat(postResult2.persons.size, is(1))
    assertThat(postResult2.persons.asJava, containsInAnyOrder(keanu))
  }


  @Test
  def testEntityRecognitionHasCanonicalRepresentationForAllCapitalsEntity() = {
    val organization = "IBM"
    val text = s"I used to work at ${organization}, it is a large company."
    val entities = underTest.recognize(text)
    Assert.assertThat(entities.organisations.size, is(1))
    Assert.assertThat(entities.organisations.asJava, hasItem(organization.toLowerCase().capitalize))
  }

  @Test
  def testEntityRecognitionHasCanonicalRepresentationForLowercaseEntity() = {
    val person = "jenny"
    val text = s"${person} said to him that he should say hello."
    val entities = underTest.recognize(text)
    Assert.assertThat(entities.persons.size, is(1))
    Assert.assertThat(entities.persons.asJava, hasItem(person.toLowerCase().capitalize))
  }
}
