package ae.teletronics.nlp.entityextraction.exclusion

import ae.teletronics.nlp.entityextraction.EntityType

/**
  * Created by Boris on 2016-05-03.
  */
class DefaultExcludeListPersister extends ExcludeListPersister {

  override def getAllExcludes(): Map[EntityType, Set[String]] = EntityType.allEntityTypes.map(et => et -> getExcludeSet(et)).toMap

  override def getExcludeSet(entityType: EntityType): Set[String] = Set()

  override def setExcludeSet(entityType: EntityType, set: Set[String]): Unit =
    throw new UnsupportedOperationException("Not implemented")

  override def addExclusion(entityType: EntityType, entity: String): Unit =
    throw new UnsupportedOperationException("Not implemented")

  override def deleteExclusion(entityType: EntityType, entity: String): Unit =
    throw new UnsupportedOperationException("Not implemented")
}
