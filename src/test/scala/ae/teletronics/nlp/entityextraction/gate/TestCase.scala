package ae.teletronics.nlp.entityextraction.gate

/**
  * Created by Boris on 2016-04-27.
  */
case class TestCase(sentence: String, persons: List[String], locations: List[String], organizations: List[String]) {
  override def toString: String = {
    sentence + "\n  Persons: " + persons.toString + "\n  Location: " + locations.toString + "\n  organizations: " + organizations
  }
}
