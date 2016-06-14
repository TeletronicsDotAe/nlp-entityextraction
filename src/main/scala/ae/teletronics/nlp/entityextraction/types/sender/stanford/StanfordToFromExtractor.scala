package ae.teletronics.nlp.entityextraction.types.sender.stanford

import ae.teletronics.nlp.entityextraction.types.sender.ToFromExtractor
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}

/**
  * Created by trym on 09-06-2016.
  */
object StanfordToFromExtractor {
  private lazy val serializedClassifier = "stanford/stanford-prod-sms-eng-per-model-fullprop.ser.gz"
  private lazy val classifier = CRFClassifier.getClassifier(serializedClassifier)
}

class StanfordToFromExtractor extends ToFromExtractor {

  import StanfordToFromExtractor.classifier

  import scala.collection.JavaConversions._

  override def process(text: String): List[String] = {
    val llcl: java.util.List[java.util.List[CoreLabel]] = classifier.classify(text)

    llcl
      .flatMap(_.map(word => (word.get(classOf[CoreAnnotations.AnswerAnnotation]), word.word())))
      .filter { case (cat, word) => cat.equals("SEND") }
      .map { case (cat, word) => word }
      .toList
  }

}
