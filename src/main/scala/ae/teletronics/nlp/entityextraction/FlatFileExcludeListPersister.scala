package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-03.
  */
class FlatFileExcludeListPersister(filenamePart: String) extends ExcludeListPersister {

  import scala.collection.JavaConversions._

  def mkFilename(entityType: String) = "exclude-list-" + entityType + "-" + filenamePart + ".txt"

  override def getExcludeList(entityType: String): java.util.List[String] = {
    import java.nio.file.{Paths, Files}

    val filename = mkFilename(entityType)

    if (Files.exists(Paths.get(filename))) {
      new java.util.ArrayList(scala.io.Source.fromFile(filename, "UTF-8").getLines.map(line => line.trim).filter(line => line != "").toList)
    } else {
      List()
    }
  }

  override def setExcludeList(entityType: String, list: java.util.List[String]): Unit = {
    import java.io.File
    import java.io.FileWriter
    import java.io.BufferedWriter

    val writer = new BufferedWriter(new FileWriter(new File(mkFilename(entityType))))

    for (entry <- list) {
      writer.write(entry)
      writer.newLine
    }
    writer.flush
    writer.close
  }
}
