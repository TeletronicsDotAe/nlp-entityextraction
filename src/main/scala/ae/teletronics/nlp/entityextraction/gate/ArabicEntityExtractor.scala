package ae.teletronics.nlp.entityextraction.gate

/**
  * Created by Boris on 2016-04-18.
  */

import java.io.File

import ae.teletronics.nlp.entityextraction.exclusion.{DefaultExcludeListPersister, ExcludeListPersister}
import ae.teletronics.nlp.entityextraction.gate.GateEntityType._
import ae.teletronics.nlp.entityextraction.model.Entities
import ae.teletronics.nlp.entityextraction.EntityExtractor
import gate._
import gate.util.persistence.PersistenceManager

import scala.collection.JavaConversions._

object ArabicEntityExtractor {
  val model = findModel()

  // initialize Gate
  Gate.runInSandbox(true)
  Gate.init()

  val corpusController: CorpusController =
    PersistenceManager
      .loadObjectFromFile(model)
      .asInstanceOf[CorpusController]

  private def findModel(): File = {
    val root = "gate-8.2/plugins/Lang_Arabic/resources/arabic.gapp"
    val locations = List("root", "conf/" + root, "src/main/resources/" + root) // this should be configurable from out side
    val foundModel = locations
      .map(new File(_))
      .find(_.exists())

    if (foundModel.isEmpty) {
      throw new IllegalStateException("Cannot find the Gate model from: " + new File(".").getAbsolutePath)
    }

    foundModel.get
  }
}

class ArabicEntityExtractor(excludePersister: ExcludeListPersister = new DefaultExcludeListPersister) extends EntityExtractor {
  import ArabicEntityExtractor.corpusController

  override def recognize(text: String): Entities = {
    val m = ArabicEntityExtractor.model // instantiate this
    val corpus: Corpus = Factory.newCorpus("corpus")
    corpus.add(Factory.newDocument(text))

    corpusController.setCorpus(corpus)
    corpusController.execute()

    extractEntities(corpus.head.getAnnotations, text)
  }

  private def extractEntities(annotations: AnnotationSet, text: String): Entities = {
    val excludes: Map[String, List[String]] = allEntityTypes.map(e => e -> excludePersister.getExcludeList(e)).toMap
    val entities: Iterable[Annotation] = annotations.get(allEntityTypes.toSet).toIterable

    val res = entities
      .map(e => (e.getType, getEntity(text, e)))
      .groupBy(_._1)
      .map { case (k, v) => (k, v.map(_._2).filter(e => !excludes(k).contains(e)).toList) }
      .withDefaultValue(List())

    Entities(res(Person), res(Location), res(Organization))
  }

  private def getEntity(text: String, a: Annotation): String = {
    text.substring(offset(a.getStartNode), offset(a.getEndNode))
  }

  private def offset(n: Node) = n.getOffset.intValue

}
