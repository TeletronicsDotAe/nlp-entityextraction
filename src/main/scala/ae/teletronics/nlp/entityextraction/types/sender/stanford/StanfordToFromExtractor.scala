package ae.teletronics.nlp.entityextraction.types.sender.stanford

import ae.teletronics.nlp.entityextraction.model.ToFrom
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}

/**
  * Created by trym on 09-06-2016.
  */
object StanfordToFromExtractor {
  private lazy val serializedClassifier = "stanford/stanford-eng-sr-model-fullprop.ser.gz"
  private lazy val classifier = CRFClassifier.getClassifier(serializedClassifier)
}

class StanfordToFromExtractor {

  import StanfordToFromExtractor.classifier

  import scala.collection.JavaConversions._

  def process(text: String): ToFrom = {
    val llcl: java.util.List[java.util.List[CoreLabel]] = classifier.classify(text)

    val toFrom: Map[String, List[String]] = llcl
      .flatMap(_.map(word => (word.get(classOf[CoreAnnotations.AnswerAnnotation]), word.word())))
      .filter { case (cat, word) => cat.equals("SEND") || cat.equals("RECV") }
      .groupBy { case (cat, word) => cat }
      .mapValues[List[String]](_.map { case (cat, word) => word }.toList)
      .withDefaultValue(List[String]())

    ToFrom(toFrom("RECV"), toFrom("SEND"))
  }

}
