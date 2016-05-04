package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-03.
  */
trait ExcludeListPersister {
  def getExcludeList: List[String]
  def setExcludeList(list: List[String]): Unit
}
