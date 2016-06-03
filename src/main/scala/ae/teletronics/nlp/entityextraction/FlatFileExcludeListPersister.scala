package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-03.
  */
class FlatFileExcludeListPersister(filenamePart: String) extends ExcludeListPersister {

  val filename = "exclude-list-" + filenamePart + ".txt"

  override def getExcludeList: List[String] = scala.io.Source.fromFile(filename).getLines.map(line => line.trim).filter(line => line != "").toList

  override def setExcludeList(list: List[String]): Unit = {
    import java.io.File
    import java.io.FileWriter
    import java.io.BufferedWriter

    val writer = new BufferedWriter(new FileWriter(new File(filename)))

    for (entry <- list) {
      writer.write(entry)
      writer.newLine
    }
    writer.flush
    writer.close
  }
}
