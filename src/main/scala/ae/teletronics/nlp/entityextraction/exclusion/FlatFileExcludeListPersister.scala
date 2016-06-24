package ae.teletronics.nlp.entityextraction.exclusion

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

import ae.teletronics.nlp.entityextraction.{Person, Location, Organization, EntityType}

/**
  * Created by Boris on 2016-05-03.
  */
class FlatFileExcludeListPersister(filenamePart: String) extends ExcludeListPersister {

  override def getAllExcludes(): Map[EntityType, Set[String]] = EntityType.allEntityTypes.map(et => et -> getExcludeSet(et)).toMap

  override def getExcludeSet(entityType: EntityType): Set[String] = {
    val filename = mkFilename(entityType)

    if (Files.exists(Paths.get(filename))) {
      scala.io.Source
        .fromFile(filename, "UTF-8")
        .getLines
        .map(_.trim)
        .filter(_.nonEmpty)
        .toSet
    } else {
      Set()
    }
  }

  override def setExcludeSet(entityType: EntityType, set: Set[String]): Unit = {
    val writer = new BufferedWriter(new FileWriter(new File(mkFilename(entityType))))

    for (entry <- set) {
      writer.write(entry)
      writer.newLine()
    }
    writer.flush()
    writer.close()
  }

  override def addExclusion(entityType: EntityType, entity: String): Unit = {
    setExcludeSet(entityType, getExcludeSet(entityType) + entity)
  }

  override def deleteExclusion(entityType: EntityType, entity: String): Unit = {
    setExcludeSet(entityType, getExcludeSet(entityType) - entity)
  }

  override def shouldExclude(t: EntityType, name: String): Boolean = {
    getExcludeSet(t).contains(name)
  }

  private def toEntityName(entityType: EntityType) = entityType match {
    case Person => "Person"
    case Location => "Location"
    case Organization => "Organization"
  }

  private def mkFilename(entityType: EntityType) = "exclude-list-" + toEntityName(entityType) + "-" + filenamePart + ".txt"

}
