package ae.teletronics.nlp.entityextraction

import ae.teletronics.nlp.entityextraction.model.Entities

/**
  * Created by Boris on 2016-04-18.
  */

trait EntityExtractor {
  def recognize(text: String): Entities
}
