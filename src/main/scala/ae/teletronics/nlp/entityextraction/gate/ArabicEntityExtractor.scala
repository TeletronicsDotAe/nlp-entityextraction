package ae.teletronics.nlp.entityextraction.gate

/**
  * Created by Boris on 2016-04-18.
  */

import java.io.File

import ae.teletronics.nlp.entityextraction.exclusion.{DefaultExcludeListPersister, ExcludeListPersister}
import ae.teletronics.nlp.entityextraction.model.Entities
import ae.teletronics.nlp.entityextraction.{Person, Location, Organization, EntityType, EntityExtractor}
import ae.teletronics.nlp.entityextraction.EntityType._
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

class ArabicEntityExtractor(excluder: ExcludeListPersister = new DefaultExcludeListPersister) extends EntityExtractor {
  import ArabicEntityExtractor.corpusController
  // The Gate Arabic entity extractor also supports the keyword "Gpe" for geopolitical entity, e.g. city, state/province, and country,
  // but the Entities return type only supports the three classes that are in the intersection of the Gate and Stanford entities,
  private val toGateName: Map[EntityType, String] = Map(Person -> "Person", Location -> "Location", Organization -> "Organization")
  private val toEntityType: Map[String, EntityType] = toGateName.map(_.swap)


  override def recognize(text: String): Entities = {
    val m = ArabicEntityExtractor.model // instantiate this
    val corpus: Corpus = Factory.newCorpus("corpus")
    corpus.add(Factory.newDocument(text))

    corpusController.setCorpus(corpus)
    corpusController.execute()

    extractEntities(corpus.head.getAnnotations, text)
  }

  private def extractEntities(annotations: AnnotationSet, text: String): Entities = {
    val res = annotations
      .get(toEntityType.keySet)
      .map(a => (a.getType, getEntity(text, a)))
      .groupBy(_._1)
      .map { case (k, vs) => (k, vs.map(_._2)) }
      .map { case (k, vs) => filter(k, vs.toList) }
      .withDefaultValue(List())

    Entities(res(toGateName(Person)), res(toGateName(Location)), res(toGateName(Organization)))
  }

  private def filter(k: String, vs:List[String]): (String, List[String]) = {
    val t = toEntityType(k)
    (k, vs.filter(!excluder.shouldExclude(t, _)))
  }

  private def getEntity(text: String, a: Annotation): String = {
    text.substring(offset(a.getStartNode), offset(a.getEndNode))
  }

  private def offset(n: Node) = n.getOffset.intValue

}
