package ae.teletronics.nlp.entityextraction.model

/**
  * Created by trym on 10-02-2016.
  */
object Entities {
  def empty() = Entities(List(), List(), List())
}

case class Entities(persons: List[String], locations: List[String], organisations: List[String]) {
  def getPersons(): List[String] = persons
  def getLocations(): List[String] = locations
  def getOrganisations(): List[String] = organisations
}
