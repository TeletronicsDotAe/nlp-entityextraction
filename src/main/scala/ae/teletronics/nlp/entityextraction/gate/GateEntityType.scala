package ae.teletronics.nlp.entityextraction.gate

/**
  * Created by Boris on 2016-04-18.
  */
object GateEntityType {
  val Person = "Person"
  val Location = "Location"
  val Organization = "Organization"
  val GeoPoliticalEntity = "Gpe" // e.g. city, state/province, and country.

  val allEntityTypes = List(Person, Location, Organization, GeoPoliticalEntity)
}