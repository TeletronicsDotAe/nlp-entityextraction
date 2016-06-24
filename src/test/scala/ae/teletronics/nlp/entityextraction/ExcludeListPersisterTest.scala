package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-04.
  */


import java.nio.file.{Files, Paths}

import org.junit._
import Assert._
import ae.teletronics.nlp.entityextraction.exclusion.{Arabic, DBExcludeListPersister, English, FlatFileExcludeListPersister}
import org.hamcrest.Matchers._
import org.mapdb.{DBMaker, Serializer}

@Test
class ExcludeListPersisterTest {

  @Test
  def testReadWriteFilePersistence() = {
    val lang = Arabic
    val subj = new FlatFileExcludeListPersister(lang)
    val entityType = Organization
    val otherEntityType = Person

    Files.deleteIfExists(Paths.get(subj.mkFilename(entityType)))
    Files.deleteIfExists(Paths.get(subj.mkFilename(otherEntityType)))

    val lines = Set("A", "BB", "CCC")

    subj.setExcludeSet(entityType, lines)
    val excludes = subj.getExcludeSet(entityType)
    val nonTypeExcludes = subj.getExcludeSet(otherEntityType)

    Files.deleteIfExists(Paths.get(subj.mkFilename(entityType)))
    Files.deleteIfExists(Paths.get(subj.mkFilename(otherEntityType)))

    assertThat(excludes.size, is(lines.size))
    assertThat(lines.size, is(3))

    assertThat(excludes.equals(lines), is(true))
    assertThat(nonTypeExcludes.size, is(0))
  }

  @Test
  def testExcludersLanguagesAreNotOverlapping() = {
    val arabic = Arabic
    val english = English
    val arabicExcluder1 = new FlatFileExcludeListPersister(arabic)
    val arabicExcluder2 = new FlatFileExcludeListPersister(arabic)
    val englishExcluder = new FlatFileExcludeListPersister(english)

    val entityType = Location

    Files.deleteIfExists(Paths.get(arabicExcluder1.mkFilename(entityType)))
    Files.deleteIfExists(Paths.get(englishExcluder.mkFilename(entityType)))

    val lines = Set("A", "BB", "CCC")

    arabicExcluder1.setExcludeSet(entityType, lines)

    val persistedArabicExcludeSet = arabicExcluder2.getExcludeSet(entityType)
    val persistedEnglishExcludeSet = englishExcluder.getExcludeSet(entityType)

    Files.deleteIfExists(Paths.get(arabicExcluder1.mkFilename(entityType)))

    assertThat(persistedArabicExcludeSet.size, is(3))
    assertThat(persistedArabicExcludeSet.equals(lines), is(true))
    assertThat(persistedEnglishExcludeSet.size, is(0))
  }

  @Test
  def testAddAndRemoveSingleEntityExclusion() = {
    val arabic = Arabic
    val entityType = Location
    val subj = new FlatFileExcludeListPersister(arabic)

    Files.deleteIfExists(Paths.get(subj.mkFilename(entityType)))

    val lines1 = Set("A", "BB", "CCC")
    val addee = "DDDD"
    val subee = "BB"
    val lines2 = lines1 + addee
    val lines3 = lines2 - subee

    subj.setExcludeSet(entityType, lines1)

    val result1 = subj.getExcludeSet(entityType)

    subj.addExclusion(entityType, addee)

    val result2 = subj.getExcludeSet(entityType)

    subj.deleteExclusion(entityType, subee)

    val result3 = subj.getExcludeSet(entityType)

    Files.deleteIfExists(Paths.get(subj.mkFilename(entityType)))

    assertThat(lines1.equals(result1), is(true))
    assertThat(lines2.equals(result2), is(true))
    assertThat(lines3.equals(result3), is(true))
  }

  @Test
  def testGetAllExcludes() = {
    val english = English
    val subj = new FlatFileExcludeListPersister(english)

    val persons = Set("P1", "P2")
    val locations = Set("L1")

    Files.deleteIfExists(Paths.get(subj.mkFilename(Person)))
    Files.deleteIfExists(Paths.get(subj.mkFilename(Location)))

    subj.setExcludeSet(Person, persons)
    subj.setExcludeSet(Location, locations)

    val excludeMap = subj.getAllExcludes()

    Files.deleteIfExists(Paths.get(subj.mkFilename(Person)))
    Files.deleteIfExists(Paths.get(subj.mkFilename(Location)))

    assertThat(excludeMap(Person).size, is(persons.size))
    assertThat(excludeMap(Person).equals(persons), is(true))
    assertThat(excludeMap(Location).size, is(locations.size))
    assertThat(excludeMap(Location).equals(locations), is(true))
    assertThat(excludeMap(Organization).size, is(0))
  }

  @Test
  def testAddExclusionsToDb(): Unit = {
    val persister = new DBExcludeListPersister(English)
    Files.deleteIfExists(Paths.get(persister.dbFile))

    assertTrue(persister.getAllExcludes().values.forall(_.isEmpty))

    persister.addExclusion(Person, "Alice")
    persister.addExclusion(Person, "Bob")
    assertEquals(2, persister.getExcludeSet(Person).size)
    assertEquals(2, persister.getAllExcludes().values.map(_.size).sum)
  }
}
