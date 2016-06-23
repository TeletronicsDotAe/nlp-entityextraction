package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-04.
  */


import org.junit._
import Assert.assertThat
import ae.teletronics.nlp.entityextraction.exclusion.{English, FlatFileExcludeListPersister, Arabic}
import org.hamcrest.Matchers._

@Test
class ExcludeListPersisterTest {

  import scala.collection.JavaConversions._

  @Test
  def testReadWriteFilePersistence() = {
    import java.nio.file.{Paths, Files}

    val lang = Arabic
    val subj = new FlatFileExcludeListPersister(lang)
    val entityType = Organization
    val otherEntityType = Person

    Files.deleteIfExists(Paths.get(subj.mkFilename(entityType)))
    Files.deleteIfExists(Paths.get(subj.mkFilename(otherEntityType)))

    val lines = List("A", "BB", "CCC")

    subj.setExcludeSet(entityType, lines.toSet)
    val excludes = subj.getExcludeSet(entityType)
    val nonTypeExcludes = subj.getExcludeSet(otherEntityType)

    Files.deleteIfExists(Paths.get(subj.mkFilename(entityType)))
    Files.deleteIfExists(Paths.get(subj.mkFilename(otherEntityType)))

    assertThat(excludes.size, is(lines.length))
    assertThat(lines.length, is(3))

    val javaLines: java.util.List[String] = lines
    val excludesList = excludes.toList
    assertThat(javaLines, containsInAnyOrder(excludesList(0), excludesList(1), excludesList(2)))
    assertThat(nonTypeExcludes.size, is(0))
  }

  @Test
  def testExcludersLanguagesAreNotOverlapping() = {
    import java.nio.file.{Paths, Files}

    val arabic = Arabic
    val english = English
    val arabicExcluder1 = new FlatFileExcludeListPersister(arabic)
    val arabicExcluder2 = new FlatFileExcludeListPersister(arabic)
    val englishExcluder = new FlatFileExcludeListPersister(english)

    val entityType = Location

    Files.deleteIfExists(Paths.get(arabicExcluder1.mkFilename(entityType)))
    Files.deleteIfExists(Paths.get(englishExcluder.mkFilename(entityType)))

    val lines = List("A", "BB", "CCC")

    arabicExcluder1.setExcludeSet(entityType, lines.toSet)

    val persistedArabicExcludeSet = arabicExcluder2.getExcludeSet(entityType)
    val persistedEnglishExcludeSet = englishExcluder.getExcludeSet(entityType)

    Files.deleteIfExists(Paths.get(arabicExcluder1.mkFilename(entityType)))

    assertThat(persistedArabicExcludeSet.size, is(3))
    assertThat(persistedArabicExcludeSet.equals(lines.toSet), is(true))
    assertThat(persistedEnglishExcludeSet.size, is(0))
  }
}
