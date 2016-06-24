package ae.teletronics.nlp.entityextraction.stanford

import ae.teletronics.nlp.entityextraction.{Person,Location,Organization,EntityType}
import ae.teletronics.nlp.entityextraction.EntityExtractor
import ae.teletronics.nlp.entityextraction.model.Entities
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}

import scala.collection.JavaConversions._

object StanfordNLPEngine {
  private lazy val serializedClassifier = "stanford/english.all.3class.distsim.crf.ser.gz"
  private lazy val classifier = CRFClassifier.getClassifier(serializedClassifier)

  private val toStanfordName: Map[EntityType, String] = Map(Person -> "PERSON", Location -> "LOCATION", Organization -> "ORGANIZATION")
  private val toEntityType: Map[String, EntityType] = toStanfordName.map(_.swap)
  private def accept(entityType: String) = toEntityType.containsKey(entityType)

  private def caseInsentiveUnique(entities: List[String]): List[String] = {
    // groupby case insensitivity:
    val groups = entities.groupBy(_.toLowerCase)

    // select good string from group, e.g. initial uppercase words
    def bestEntity(list: List[String]): String = {
      if (!list.exists(_.charAt(0).isUpper))
        list(0)
      else
        list.dropWhile(e => !e.charAt(0).isUpper)(0)
    }

    groups.values.map(bestEntity(_)).toList
  }

}

class StanfordNLPEngine extends EntityExtractor {
  import StanfordNLPEngine._

  def recognize(text: String): Entities = {
    val llcl: java.util.List[java.util.List[CoreLabel]] = classifier.classify(text)

    val entities: Map[String, List[String]] = llcl
      .flatMap(_.map(word => (word.get(classOf[CoreAnnotations.AnswerAnnotation]), word.word())))
      .sliding(2) // must concat names of same category in sequence
      .foldLeft(Map[String, List[String]]()) { case (acc, buf) => {
        val (cat1, word1) = buf.head
        val (cat2, word2) = if (buf.size > 1) buf(1) else ("", "")
        if (accept(cat1)) { // found an entity
          val name = if (cat2.equals(cat1)) word1 + " " + word2 else word1

          val catNames = acc.getOrElse[List[String]](cat1, List())
          val newCatNames: List[String] = if (catNames.lastOption.exists(_.contains(word1))) {
            val newLast = catNames.last + (if (cat2.equals(cat1)) " " + word2 else "")
            catNames.init :+ newLast
          } else {
            catNames :+ name
          }
          acc + (cat1 -> newCatNames)
        } else {
          acc
        }}}
      .mapValues(caseInsentiveUnique(_))
      .withDefaultValue(List())

    Entities(entities(toStanfordName(Person)), entities(toStanfordName(Location)), entities(toStanfordName(Organization)))
  }

}