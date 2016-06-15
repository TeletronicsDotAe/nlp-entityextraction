package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-04.
  */


import org.junit._
import Assert.assertThat
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

    subj.setExcludeList(lines)
    val excludes = subj.getExcludeList
    Files.deleteIfExists(Paths.get(filename))

    assertThat(excludes.length, is(lines.length))
    assertThat(lines.length, is(3))

    val javaLines: java.util.List[String] = lines
    assertThat(javaLines, containsInAnyOrder(excludes(0), excludes(1), excludes(2)))
  }
}
