package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-04-18.
  */

trait EntityExtractor {
  def recognize(text: String): java.util.Map[String, java.util.List[String]]
}
