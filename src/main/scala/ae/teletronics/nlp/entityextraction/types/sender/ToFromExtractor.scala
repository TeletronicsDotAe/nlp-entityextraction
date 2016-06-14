package ae.teletronics.nlp.entityextraction.types.sender

/**
  * Created by hhravn on 10/06/16.
  */
trait ToFromExtractor {
  def process(text: String): List[String]
}
