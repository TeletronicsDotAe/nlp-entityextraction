package ae.teletronics.nlp.entityextraction.exclusion

/**
  * Created by Boris on 2016-05-03.
  */
trait ExcludeListPersister {
  def getExcludeList(entityType: String): List[String]
  def setExcludeList(entityType: String, list: List[String]): Unit
}
