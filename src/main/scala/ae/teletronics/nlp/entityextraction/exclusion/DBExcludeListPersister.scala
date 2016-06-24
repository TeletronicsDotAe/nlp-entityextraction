package ae.teletronics.nlp.entityextraction.exclusion

import ae.teletronics.nlp.entityextraction.EntityType
import org.mapdb.{DBMaker, Serializer}

import scala.collection.JavaConversions._

class DBExcludeListPersister(exclusionLanguage: ExclusionLanguage) extends ExcludeListPersister {
  val dbFile = s"exclude-list-$exclusionLanguage.db"

  private def handleExclusions[T](entityType: EntityType)(action: java.util.Set[String] => T): T = {
    val db = DBMaker.fileDB(dbFile).make
    val set = db.hashSet(entityType.toString, Serializer.STRING).createOrOpen()
    try {
      action(set)
    } finally {
      db.close()
    }
  }

  override def getAllExcludes(): Map[EntityType, Set[String]] =
    EntityType.allEntityTypes.map(et => et -> getExcludeSet(et)).toMap

  override def addExclusion(entityType: EntityType, entity: String): Unit = handleExclusions(entityType)(_.add(entity))

  override def getExcludeSet(entityType: EntityType): Set[String] = handleExclusions(entityType)(_.toSet)

  override def deleteExclusion(entityType: EntityType, entity: String): Unit =
    handleExclusions(entityType)(_.remove(entity))

  override def setExcludeSet(entityType: EntityType, set: Set[String]): Unit =
    handleExclusions(entityType)(_.addAll(set))

  override def isExcluded(t: EntityType, name: String): Boolean = getExcludeSet(t).contains(name)
}

