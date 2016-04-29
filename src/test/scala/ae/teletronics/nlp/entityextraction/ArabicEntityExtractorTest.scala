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
    val subj = ArabicEntityExtractor
    val result = subj.recognize("")

    assertThat(result(EntityType.Person).length, is(0))
    assertThat(result(EntityType.Location).length, is(0))
    assertThat(result(EntityType.Organization).length, is(0))
  }

  @Test
  def testCorrectEntityExtraction() = {
    val subj = ArabicEntityExtractor

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
  def testAnotherCorrectEntityRecognition() = {
    val subj = ArabicEntityExtractor

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
  def testBlah() = {
    val subj = ArabicEntityExtractor
    val testcases = TestCaseReader.readTestCases
    val results = testcases.map(tc => subj.recognize(tc.sentence))

    for ((testCase, result) <- testcases.zip(results)) {
      val result = subj.recognize(testCase.sentence)
      val persons = result(EntityType.Person).toSet
      val locations = result(EntityType.Location).toSet
      val organizations = result(EntityType.Organization).toSet

      val correctPersons = testCase.persons.toSet
      val correctLocations = testCase.locations.toSet
      val correctOrganizations = testCase.organizations.toSet
/*
      if (persons != correctPersons)
        println("Got: " + persons.toString + ", expected: " + correctPersons.toString)
      if (locations != correctLocations)
        println("Got: " + locations.toString + ", expected: " + correctLocations.toString)
      if (organizations != correctOrganizations)
        println("Got: " + organizations.toString + ", expected: " + correctOrganizations.toString)
*/
    }

    def isCorrect(tpl: Tuple2[TestCase, java.util.Map[String, java.util.List[String]]]): Boolean = {
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
    println(testcases.zip(results).filter(isCorrect).length)
    println(testcases.zip(results).filter(tpl => isCorrect(tpl) && !isEmpty(tpl._1)).length)



    assertThat(0, is(0))
  }

  @Test
  def testNewLastName() = {
    val subj = ArabicEntityExtractor

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
