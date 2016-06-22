package ae.teletronics.nlp.entityextraction.exclusion

import ae.teletronics.nlp.entityextraction.EntityType

/**
  * Created by Boris on 2016-05-03.
  */
trait ExcludeListPersister {
  def getAllExcludes(): Map[EntityType, Set[String]]
  def getExcludeSet(entityType: EntityType): Set[String]
  def setExcludeSet(entityType: EntityType, set: Set[String]): Unit
  def addExclusion(entityType: EntityType, entity: String): Unit
  def deleteExclusion(entityType: EntityType, entity: String): Unit
}
