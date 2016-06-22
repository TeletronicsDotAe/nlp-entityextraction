package ae.teletronics.nlp.entityextraction.exclusion

/**
  * Created by Boris on 2016-05-03.
  */
class DefaultExcludeListPersister extends ExcludeListPersister {

  override def getExcludeList(entityType: String): List[String] = List[String]()

  override def setExcludeList(entityType: String, list: List[String]) =
    throw new UnsupportedOperationException("Not implemented")
}
