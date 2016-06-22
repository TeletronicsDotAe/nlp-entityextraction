package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-04-18.
  */

import ae.teletronics.nlp.entityextraction.exclusion.FlatFileExcludeListPersister
import ae.teletronics.nlp.entityextraction.gate.{ArabicEntityExtractor, GateEntityType}
import ae.teletronics.nlp.entityextraction.model.Entities
import org.hamcrest.Matchers._
import org.junit.Assert.assertThat
import org.junit._

import scala.collection.JavaConverters._

@Ignore
@Test
class ArabicEntityExtractorTest {

  @Test
  def testIsTotalFunction() = {
    val subj = new ArabicEntityExtractor
    val result = subj.recognize("")

    assertThat(result.persons.length, is(0))
    assertThat(result.locations.length, is(0))
    assertThat(result.organisations.length, is(0))
  }

  @Test
  def testCorrectEntityExtraction() = {
    val subj = new ArabicEntityExtractor

    /* The following sentence comes from the Arabic Wikipedia article for Yasser Arafat
              (http://ar.wikipedia.org/wiki/%D9%8A%D8%A7%D8%B3%D8%B1_%D8%B9%D8%B1%D9%81%D8%A7%D8%AA).
              Google Translate tells us it means something like "At the end of 2004,
              Yasser Arafat's illness after two years of siege in his headquarters
              in Ramallah by the Israeli army, and went into a coma." in English.
            */
    val text = "وفي نهايات عام 2004 مرض ياسر عرفات بعد سنتين من حصاره داخل مقره في رام الله من قبل الجيش الإسرائيلي، ودخل في غيبوبة."
    val result = subj.recognize(text)

    val person = "ياسر عرفات"
    val location = "رام الله"
    val organization = "الجيش الإسرائيلي"
    assertThat(result.persons.length, is(1))

    assertThat(result.persons.asJava, contains(person))

    assertThat(result.locations.length, is(1))
    assertThat(result.locations.asJava, contains(location))

    assertThat(result.organisations.length, is(1))
    assertThat(result.organisations.asJava, contains(organization))
  }

  @Test
  def testFileExcludeListPersisterExclusion() = {
    val text = "وفي نهايات عام 2004 مرض ياسر عرفات بعد سنتين من حصاره داخل مقره في رام الله من قبل الجيش الإسرائيلي، ودخل في غيبوبة."

    // this would normally be detected as a person in the text, as per the testCorrectEntityExtraction unit test above
    val person = "ياسر عرفات"

    val preSubj = new ArabicEntityExtractor
    val preResult = preSubj.recognize(text)
    assertThat(preResult.persons.length, is(1))
    assertThat(preResult.persons.asJava, containsInAnyOrder(person))

    val excludeFileName = "arabicTest"

    val excluder = new FlatFileExcludeListPersister(excludeFileName)
    excluder.setExcludeList(GateEntityType.Person, List(person))

    val postSubj = new ArabicEntityExtractor(excluder)
    val postResult = postSubj.recognize(text)

    import java.nio.file.{Files, Paths}
    Files.deleteIfExists(Paths.get(excludeFileName))

    assertThat(postResult.persons.size, is(0))
  }

  @Test
  @Ignore("The Arabic NER cannot find any of these entities")
  def testAnotherCorrectEntityRecognition() = {
    val subj = new ArabicEntityExtractor

    val text = "يعمل غيث في تيليترونكس في دبي"
    val result = subj.recognize(text)

    val dubai = "دبي"
    val ghaith = "غيث"
    val teletronics = "تيليترونكس"

    println(result)

    assertThat(result.persons.length, is(1))
    assertThat(result.persons.asJava, contains(ghaith))

    assertThat(result.locations.length, is(1))
    assertThat(result.locations.asJava, contains(dubai))

    assertThat(result.organisations.length, is(1))
    assertThat(result.organisations.asJava, contains(teletronics))
  }

  @Test
  @Ignore("This is not a unit test, but more of a test to find the performance of the entity recognizer")
  def testPrecisionAndRecall() = {
    val subj = new ArabicEntityExtractor
    //    val testcases = TestCaseReader.readANERCorpTestCases
    val testcases = TestCaseReader.readAQMARCorpTestCases
    val results = testcases.map(tc => subj.recognize(tc.sentence)).toArray

    println(testcases.length)
    println(testcases.zip(results).count(isCorrect))
    println(testcases.zip(results).count(tpl => isCorrect(tpl) && !isEmpty(tpl._1)))

    println("++++++++++++++++++++++ BEGIN examples ++++++++++++++++++++++++")

    for ((testCase, result) <- testcases.zip(results).filter(tpl => !isCorrect(tpl)).take(10)) {
      val persons = result.persons.toSet
      val locations = result.locations.toSet
      val organizations = result.organisations.toSet

      val correctPersons = testCase.persons.toSet
      val correctLocations = testCase.locations.toSet
      val correctOrganizations = testCase.organizations.toSet


      println("--------------------------------")
      println(testCase.sentence)
      println("  correct persons: " + correctPersons.toString)
      println("  found persons:   " + persons.toString)
      println("  correct locations: " + correctLocations.toString)
      println("  found locations:   " + locations.toString)
      println("  correct organizations: " + correctOrganizations.toString)
      println("  found organizations:   " + organizations.toString)
    }

    case class CustomEqualsString(string: String) {
      override def toString = string

      override def hashCode = string.hashCode

      override def equals(o: Any) = o match {
        case that: CustomEqualsString => {
          val thoseWords = that.string.split(' ').toSet
          val theseWords = this.string.split(' ').toSet
          theseWords.intersect(thoseWords).nonEmpty
        }
        case _ => false
      }
    }

    println("++++++++++++++++++++ END examples ++++++++++++++++++++++++++++++")

    def printStats(headline: String, correctEntities: List[Set[CustomEqualsString]], resultEntities: List[Set[CustomEqualsString]]): Unit = {
      val entityTuples = correctEntities.zip(resultEntities)
      val correctResultEntities = entityTuples.map(tpl => tpl._1.intersect(tpl._2))
      val correctEntitiesCount = correctEntities.map(s => s.size).sum
      val resultEntitiesCount = resultEntities.map(s => s.size).sum
      val correctResultEntitiesCount = correctResultEntities.map(s => s.size).sum

      println("----------" + headline + "-----------")

      println("agreeing examples: " + entityTuples.count(tpl => tpl._1 == tpl._2))
      println("agreeing examples, nonempty && subset result: " + entityTuples.count(tpl => {
        val correctEntities = tpl._1
        val resultEntities = tpl._2
        resultEntities.nonEmpty && resultEntities.subsetOf(correctEntities)
      }))
      println("agreeing examples, nonempty && subset correct: " + entityTuples.count(tpl => {
        val correctEntities = tpl._1
        val resultEntities = tpl._2
        correctEntities.nonEmpty && correctEntities.subsetOf(resultEntities)
      }))
      println("correctEntities: " + correctEntitiesCount)
      println("resultEntities: " + resultEntitiesCount)
      println("correctResultEntities: " + correctResultEntitiesCount)
      println("recall: " + (correctResultEntitiesCount.toDouble / correctEntitiesCount))
      println("precision: " + (correctResultEntitiesCount.toDouble / resultEntitiesCount))
    }

    val correctPersons = testcases.map(tc => tc.persons.map(CustomEqualsString).toSet)
    val resultPersons = results.map(r => r.persons.map(CustomEqualsString).toSet).toList

    val correctLocations = testcases.map(tc => tc.locations.map(CustomEqualsString).toSet)
    val resultLocations = results.map(r => r.locations.map(CustomEqualsString).toSet).toList

    val correctOrganizations = testcases.map(tc => tc.organizations.map(CustomEqualsString).toSet)
    val resultOrganizations = results.map(r => r.organisations.map(CustomEqualsString).toSet).toList

    val allCorrectEntities = correctPersons ++ correctLocations ++ correctOrganizations
    val allResultEntities = resultPersons ++ resultLocations ++ resultOrganizations

    printStats("Persons", correctPersons, resultPersons)
    printStats("Locations", correctLocations, resultLocations)
    printStats("Organizatons", correctOrganizations, resultOrganizations)
    printStats("All entities", allCorrectEntities, allResultEntities)

    assertThat(0, is(0))
  }

  private def isCorrect(tpl: (TestCase, Entities)): Boolean = {
    val (testCase, result) = tpl

    (result.persons.toSet == testCase.persons.toSet
      && result.locations.toSet == testCase.locations.toSet
      && result.organisations.toSet == testCase.organizations.toSet)
  }

  private def isEmpty(testCase: TestCase): Boolean = {
    testCase match {
      case TestCase(_, Nil, Nil, Nil) => true
      case _ => false
    }
  }


}
