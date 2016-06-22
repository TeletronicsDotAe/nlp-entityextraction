package ae.teletronics.nlp.entityextraction.exclusion

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

/**
  * Created by Boris on 2016-05-03.
  */
class FlatFileExcludeListPersister(filenamePart: String) extends ExcludeListPersister {

  def mkFilename(entityType: String) = "exclude-list-" + entityType + "-" + filenamePart + ".txt"

  override def getExcludeList(entityType: String): List[String] = {
    val filename = mkFilename(entityType)

    if (Files.exists(Paths.get(filename))) {
      scala.io.Source
        .fromFile(filename, "UTF-8")
        .getLines
        .map(_.trim)
        .filter(_.nonEmpty)
        .toList
    } else {
      List()
    }
  }

  override def setExcludeList(entityType: String, list: List[String]): Unit = {
    val writer = new BufferedWriter(new FileWriter(new File(mkFilename(entityType))))

    for (entry <- list) {
      writer.write(entry)
      writer.newLine()
    }
    writer.flush()
    writer.close()
  }
}
