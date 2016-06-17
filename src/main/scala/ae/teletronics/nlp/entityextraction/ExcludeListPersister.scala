package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-03.
  */
trait ExcludeListPersister {
  def getExcludeList: java.util.List[String]
  def setExcludeList(list: java.util.List[String]): Unit
}
