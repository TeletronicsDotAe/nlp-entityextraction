package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-04.
  */


import org.junit._
import Assert.assertThat
import ae.teletronics.nlp.entityextraction.exclusion.FlatFileExcludeListPersister
import ae.teletronics.nlp.entityextraction.gate.GateEntityType
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

    subj.setExcludeList(GateEntityType.Location, lines)
    val excludes = subj.getExcludeList(GateEntityType.Location)
    val nonTypeExcludes = subj.getExcludeList(GateEntityType.Organization)
    Files.deleteIfExists(Paths.get(filename))

    assertThat(excludes.length, is(lines.length))
    assertThat(lines.length, is(3))

    val javaLines: java.util.List[String] = lines
    assertThat(javaLines, containsInAnyOrder(excludes(0), excludes(1), excludes(2)))
    assertThat(nonTypeExcludes.size(), is(0))
  }
}
