package ae.teletronics.nlp.entityextraction.stanford

/**
  * Created by trym on 21-06-2016.
  */
object StanfordEntityType {
  val Person = "PERSON"
  val Location = "LOCATION"
  val Organization = "ORGANIZATION"

  def accept(category: String): Boolean = {
    category == Person || category == Organization || category == Location
  }

}
