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
}
