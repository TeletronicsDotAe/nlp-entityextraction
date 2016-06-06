package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-04-18.
  */
import gate.Annotation
import gate.Corpus
import gate.AnnotationSet
import gate.CorpusController
import gate.Factory
import gate.Gate
import gate.util.persistence.PersistenceManager

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

class ArabicEntityExtractor(excludeListPersister: ExcludeListPersister) extends EntityExtractor {

  def this() = this(new DefaultExcludeListPersister)

  val annie: CorpusController = PersistenceManager.loadObjectFromFile(new java.io.File("")).asInstanceOf[CorpusController]

  override def recognize(text: String): java.util.Map[String, java.util.List[String]] = {

    val corpus: Corpus = Factory.newCorpus("corpus")
    corpus.add(Factory.newDocument(text))
    annie.setCorpus(corpus)
    annie.execute()

    extractEntities(corpus.get(0).getAnnotations, text).withDefaultValue(List[String]())
  }

  private def extractEntities(annotations: AnnotationSet, text: String): java.util.Map[String, java.util.List[String]] = {

    val excludes = excludeListPersister.getExcludeList

    val entities: Iterable[Annotation] = annotations.get(Set[String](EntityType.Person, EntityType.Location, EntityType.GeoPoliticalEntity, EntityType.Organization)).toIterable

    def getEntity(entity: Annotation): String = {
      text.substring(entity.getStartNode.getOffset.intValue, entity.getEndNode.getOffset.intValue())
    }

    val ret: java.util.Map[String, java.util.List[String]] = entities.map(e => e.getType -> getEntity(e))
      .groupBy(_._1)
      .map { case (k, v) => (k, v.map(_._2).filter(e => !excludes.contains(e)).toList.asJava) }

    ret
  }
}

object ArabicEntityExtractor {
  Gate.runInSandbox(true)
  Gate.init()

  val defaultModelName = "src/main/resources/plugins/Lang_Arabic/resources/arabic.gapp"
}
