package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-05-03.
  */
class DefaultExcludeListPersister extends ExcludeListPersister {

  import scala.collection.JavaConversions._

  override def getExcludeList(entityType: String): java.util.List[String] = List[String]()

  override def setExcludeList(entityType: String, list: java.util.List[String]): Unit = return
}
