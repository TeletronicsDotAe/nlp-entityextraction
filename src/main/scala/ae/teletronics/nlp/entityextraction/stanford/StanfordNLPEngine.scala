package ae.teletronics.nlp.entityextraction.stanford

import ae.teletronics.nlp.entityextraction.exclusion.{DefaultExcludeListPersister, ExcludeListPersister}
import ae.teletronics.nlp.entityextraction.{Person,Location,Organization,EntityType}
import ae.teletronics.nlp.entityextraction.EntityExtractor
import ae.teletronics.nlp.entityextraction.model.Entities
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}

import scala.collection.JavaConversions._

object StanfordNLPEngine {
  private lazy val serializedClassifier = "stanford/english.all.3class.distsim.crf.ser.gz"
  private lazy val classifier = CRFClassifier.getClassifier(serializedClassifier)

  private val StanfordPerson = "PERSON"
  private val StanfordLocation = "LOCATION"
  private val StanfordOrganization = "ORGANIZATION"

  private val StanfordEntities = Set(StanfordPerson, StanfordLocation, StanfordOrganization)
  private def toEntityType(gateEntity: String): EntityType = {
    gateEntity match {
      case StanfordPerson => Person
      case StanfordLocation => Location
      case StanfordOrganization => Organization
      case _ => throw new IllegalArgumentException("argument must be in " + StanfordEntities.toString + ", but was: " + gateEntity)
    }
  }
  private def accept(entityType: String) = StanfordEntities.contains(entityType)

  // The canonical representation of an entity is initial capitalization for each word, concatenated with a single space, e.g. "New York"
  // This will ensure that when an entity is put in the entities result object, there will only be one representation,
  // so different messages with e.g. "New York" and "new york" will count as having the same entity, and this will
  // solve the problem in the GUI of showing "New York" and "new york" in for instance an entity cloud or a list.
  // This approach has the drawback of converting e.g. "IBM" to "Ibm", but it is not easy to see a good solution that
  // will keep "IBM" as "IBM", while at the same time counting "ibm" as the same entity.
  private def canonicalEntity(nonCanonicalEntity: String): String = {
    nonCanonicalEntity.toLowerCase.capitalize
  }
}

class StanfordNLPEngine(excluder: ExcludeListPersister = new DefaultExcludeListPersister) extends EntityExtractor {
  import StanfordNLPEngine._

  def recognize(text: String): Entities = {
    // a list of list of corelabels correspond to a text that can have multiple sentences
    val llcl: java.util.List[java.util.List[CoreLabel]] = classifier.classify(text)

    val entities: Map[EntityType, List[String]] = llcl
      .flatMap(groupEqualConsecutiveElements(_))
      .filter(tpl => accept(tpl._1))
      .map { case ( annotation, words ) => (toEntityType(annotation), words.map(canonicalEntity(_)).mkString(" ")) }
      .filter { case (entityType, entity) => !excluder.isExcluded(entityType, entity) }
      .groupBy(_._1)
      .mapValues(v => v.map(_._2).toList)
      .withDefaultValue(List())

    Entities(entities(Person), entities(Location), entities(Organization))
  }

  // a list of corelabels correspond to a single sentence. Group those consecutive corelabels together that have the same AnswerAnnotation
  // e.g. [('Person', 'firstName1'), ('Person', 'middleName1'), ('Person', 'lastName1'), ('Nothing', 'and'), ('Person', 'firstName2'), ('Person', 'lastName2'))]
  // becomes
  // [('Person', ['firstName1', 'middleName1', 'lastName1']), ('Nothing', ['and']), ('Person', ['firstName2', 'lastName2'])]
  private def groupEqualConsecutiveElements(labels: java.util.List[CoreLabel]): List[(String, List[String])] = {
    labels
      .map(cl => (cl.get(classOf[CoreAnnotations.AnswerAnnotation]), cl.word()))
      .foldLeft(List[(String, List[String])]()) { case (acc, (annotation, word)) =>
        acc match {
          case ((runningAnnotation, words) :: tuples) if (annotation == runningAnnotation) => (runningAnnotation, words :+ word) :: tuples
          case tuples => (annotation, List(word)) :: tuples
        }
      }.reverse
  }
}