package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-04-18.
  */

import org.junit._
import Assert.assertThat
import org.hamcrest.Matchers._
import scala.collection.JavaConversions._

@Test
class ArabicEntityExtractorTest {

  @Test
  def testIsTotalFunction() = {
    val subj = new ArabicEntityExtractor
    val result = subj.recognize("")

    assertThat(result(EntityType.Person).length, is(0))
    assertThat(result(EntityType.Location).length, is(0))
    assertThat(result(EntityType.Organization).length, is(0))
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
    assertThat(result(EntityType.Person).length, is(1))
    assertThat(result(EntityType.Person), contains(person))

    assertThat(result(EntityType.Location).length, is(1))
    assertThat(result(EntityType.Location), contains(location))

    assertThat(result(EntityType.Organization).length, is(1))
    assertThat(result(EntityType.Organization), contains(organization))
  }

  @Test
  def testFileExcludeListPersisterExclusion() = {
    val text = "وفي نهايات عام 2004 مرض ياسر عرفات بعد سنتين من حصاره داخل مقره في رام الله من قبل الجيش الإسرائيلي، ودخل في غيبوبة."

    // this would normally be detected as a person in the text, as per the testCorrectEntityExtraction unit test above
    val person = "ياسر عرفات"

    val preSubj = new ArabicEntityExtractor
    val preResult = preSubj.recognize(text)
    assertThat(preResult(EntityType.Person).size(), is(1))
    assertThat(preResult(EntityType.Person), containsInAnyOrder(person))

    val excludeFileName = "arabicTest"

    val excluder = new FlatFileExcludeListPersister(excludeFileName)
    excluder.setExcludeList(List(person))

    val postSubj = new ArabicEntityExtractor(excluder)
    val postResult = postSubj.recognize(text)

    import java.nio.file.{Paths, Files}
    Files.deleteIfExists(Paths.get(excludeFileName))

    assertThat(postResult(EntityType.Person).size, is(0))
  }

  @Test
  def testAnotherCorrectEntityRecognition() = {
    val subj = new ArabicEntityExtractor

    val text = "يعمل غيث في تيليترونكس في دبي"
    val result = subj.recognize(text)

    val dubai = "دبي"
    val ghaith = "غيث"
    val teletronics = "تيليترونكس"

    println(result)

    assertThat(result(EntityType.Person).length, is(1))
    assertThat(result(EntityType.Person), contains(ghaith))

    assertThat(result(EntityType.Location).length, is(1))
    assertThat(result(EntityType.Location), contains(dubai))

    assertThat(result(EntityType.Organization).length, is(1))
    assertThat(result(EntityType.Organization), contains(teletronics))
  }

  @Test
  def testPrecisionAndAccuracy() = {
    val subj = new ArabicEntityExtractor
    val testcases = TestCaseReader.readTestCases
    val results = testcases.map(tc => subj.recognize(tc.sentence)).toArray

    def isCorrect(tpl: (TestCase, java.util.Map[String, java.util.List[String]])): Boolean = {
      val (testCase, result) = tpl
      result(EntityType.Person).toSet == testCase.persons.toSet && result(EntityType.Location).toSet == testCase.locations.toSet && result(EntityType.Organization).toSet == testCase.organizations.toSet
    }

    def isEmpty(testCase: TestCase): Boolean = {
      testCase match {
        case TestCase(_, Nil, Nil, Nil) => true
        case _ => false
      }
    }

    println(testcases.length)
    println(testcases.zip(results).count(isCorrect))
    println(testcases.zip(results).count(tpl => isCorrect(tpl) && !isEmpty(tpl._1)))

    println("++++++++++++++++++++++ BEGIN examples ++++++++++++++++++++++++")

    for ((testCase, result) <- testcases.zip(results).filter(tpl => !isCorrect(tpl)).take(10)) {
      val persons = result(EntityType.Person).toSet
      val locations = result(EntityType.Location).toSet
      val organizations = result(EntityType.Organization).toSet

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

    println("++++++++++++++++++++ END examples ++++++++++++++++++++++++++++++")

    def printStats(correctEntities: List[Set[String]], resultEntities: List[Set[String]]): Unit = {
      val entityTuples = correctEntities.zip(resultEntities)
      val correctResultEntities = entityTuples.map(tpl => tpl._1.intersect(tpl._2))
      val correctEntitiesCount = correctEntities.map(s => s.size).sum
      val resultEntitiesCount = resultEntities.map(s => s.size).sum
      val correctResultEntitiesCount = correctResultEntities.map(s => s.size).sum

      println("---------------------")

      println(entityTuples.count(tpl => tpl._1 == tpl._2))
      println(entityTuples.count(tpl => {
        val correctEntities = tpl._1
        val resultEntities = tpl._2
        resultEntities.nonEmpty && resultEntities.subsetOf(correctEntities)
      }))
      println(entityTuples.count(tpl => {
        val correctEntities = tpl._1
        val resultEntities = tpl._2
        correctEntities.nonEmpty && correctEntities.subsetOf(resultEntities)
      }))
      println(correctEntitiesCount)
      println(resultEntitiesCount)
      println(correctResultEntitiesCount)
      println("recall: " + (correctResultEntitiesCount.toDouble / correctEntitiesCount))
      println("precision: " + (correctResultEntitiesCount.toDouble / resultEntitiesCount))
    }


    val correctPersons = testcases.map(tc => tc.persons.toSet)
    val resultPersons = results.map(r => r(EntityType.Person).toSet).toList

    val correctLocations = testcases.map(tc => tc.locations.toSet)
    val resultLocations = results.map(r => r(EntityType.Location).toSet).toList

    val correctOrganizations = testcases.map(tc => tc.organizations.toSet)
    val resultOrganizations = results.map(r => r(EntityType.Organization).toSet).toList

    val allCorrectEntities = correctPersons ++ correctLocations ++ correctOrganizations
    val allResultEntities = resultPersons ++ resultLocations ++ resultOrganizations

    printStats(correctPersons, resultPersons)
    printStats(correctLocations, resultLocations)
    printStats(correctOrganizations, resultOrganizations)
    printStats(allCorrectEntities, allResultEntities)

    assertThat(0, is(0))
  }



  @Test
  def testNewLastName() = {
    val subj = new ArabicEntityExtractor

    val text = "Hej"
    val result = subj.recognize(text)

    import scala.collection.JavaConverters._

    println(result.asScala)

    val person = "ياسر عرفات"
    val location = "رام الله"
    val organization = "الجيش الإسرائيلي"
    assertThat(result(EntityType.Person).length, is(1))
    assertThat(result(EntityType.Person), contains(person))

    assertThat(result(EntityType.Location).length, is(1))
    assertThat(result(EntityType.Location), contains(location))

    assertThat(result(EntityType.Organization).length, is(1))
    assertThat(result(EntityType.Organization), contains(organization))
  }
}
