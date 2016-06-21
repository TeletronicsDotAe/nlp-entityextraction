package ae.teletronics.nlp.entityextraction.stanford

import ae.teletronics.nlp.entityextraction.model.Entities
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}

import scala.collection.JavaConversions._
import scala.collection.mutable

object StanfordNLPEngine {
  private lazy val serializedClassifier = "stanford/english.all.3class.distsim.crf.ser.gz"
  private lazy val classifier = CRFClassifier.getClassifier(serializedClassifier)
}

class StanfordNLPEngine {

  import StanfordNLPEngine.classifier

  def process(text: String): Entities = {
    val llcl: java.util.List[java.util.List[CoreLabel]] = classifier.classify(text)

    val entities: Map[String, List[String]] = llcl
      .flatMap(_.map(word => (word.get(classOf[CoreAnnotations.AnswerAnnotation]), word.word())))
      .sliding(2) // must concat names of same category in sequence
      .foldLeft(Map[String, List[String]]()) { case (acc, mutable.Buffer((cat1, word1), (cat2, word2))) =>
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
        }
      }
      .withDefaultValue[List[String]](List[String]())

    Entities(entities("PERSON"), entities("LOCATION"), entities("ORGANIZATION"))
  }

  private def accept(category: String): Boolean = {
    category == "PERSON" || category == "ORGANIZATION" || category == "LOCATION"
  }
}