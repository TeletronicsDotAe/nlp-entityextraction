package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-03.
  */
class DefaultExcludeListPersister extends ExcludeListPersister {
  override def getExcludeList: List[String] = List[String]()

  override def setExcludeList(list: List[String]): Unit = return
}
