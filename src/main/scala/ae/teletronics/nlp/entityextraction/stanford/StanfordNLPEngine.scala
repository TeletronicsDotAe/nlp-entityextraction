package ae.teletronics.nlp.entityextraction.stanford

import ae.teletronics.nlp.entityextraction.model.Entities
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}

import scala.collection.JavaConversions._

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
      .filter { case (cat, word) => accept(cat) }
      .groupBy(_._1)
      .map(e => (e._1, e._2.map(_._2).toList))
      .withDefaultValue[List[String]](List[String]())
    //      .withDefaultValue(new util.ArrayList[String]())

    Entities(entities("PERSON"), entities("LOCATION"), entities("ORGANIZATION"))
  }

  private def accept(category: String): Boolean = {
    category == "PERSON" || category == "ORGANIZATION" || category == "LOCATION"
  }
}