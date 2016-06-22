package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-04.
  */


import org.junit._
import Assert.assertThat
import ae.teletronics.nlp.entityextraction.exclusion.FlatFileExcludeListPersister
import ae.teletronics.nlp.entityextraction.{Person,Location,Organization,EntityType}
import org.hamcrest.Matchers._

import scala.collection.JavaConversions._

@Test
class ExcludeListPersisterTest {

  import scala.collection.JavaConversions._

  @Test
  def testReadWriteFilePersistence() = {
    import java.nio.file.{Paths, Files}

    val filename = "arabicTest"

    Files.deleteIfExists(Paths.get(filename))

    val lines = List("A", "BB", "CCC")

    val subj = new FlatFileExcludeListPersister(filename)

    subj.setExcludeSet(Location, lines.toSet)
    val excludes = subj.getExcludeSet(Location)
    val nonTypeExcludes = subj.getExcludeSet(Organization)
    Files.deleteIfExists(Paths.get(filename))

    assertThat(excludes.size, is(lines.length))
    assertThat(lines.length, is(3))

    val javaLines: java.util.List[String] = lines
    val excludesList = excludes.toList
    assertThat(javaLines, containsInAnyOrder(excludesList(0), excludesList(1), excludesList(2)))
    assertThat(nonTypeExcludes.size, is(0))
  }
}
