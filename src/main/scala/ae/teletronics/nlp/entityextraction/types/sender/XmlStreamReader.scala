package ae.teletronics.nlp.entityextraction.types.sender

import java.io.InputStream
import java.util

import ae.teletronics.nlp.entityextraction.stanford.StanfordNLPEngine
import ae.teletronics.nlp.entityextraction.types.sender.DoubleUtil.asDouble
import opennlp.tools.tokenize.SimpleTokenizer
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
class XmlStreamReader(context: SparkContext, tokenizer: SimpleTokenizer) {
  private val engine = new StanfordNLPEngine()

  def read(msgs: List[TrainMessage]): RDD[LabeledPoint] = {
    val messages = msgs.flatMap(t => {
      val persons = engine.process(t.content).getPersons()
      val contentAsTokens = tokenizer.tokenize("." + t.content)
      val tokenCount = contentAsTokens.length
      contentAsTokens
        .iterator.sliding(2)
        .zipWithIndex
        .map { case (terms, index) => asFeature(terms(1), (tokenCount - index), terms(0), persons) }
        .map(f => {
          val aIsSender = asDouble(isSender(f, t.s, (tokenCount - t.sPos)))
          //            println(s"isSender: ${aIsSender}, f: ${f.features}")
          LabeledPoint(aIsSender, f.features)
        })
    })

    context.makeRDD(messages)

  }

  def read(trainingXmlStream: InputStream): RDD[LabeledPoint] =
    read(TrainMessage.readMessages(trainingXmlStream).toList)

  private def asFeature(term: String, pos: Int, previousTerm: String, persons: List[String]): SenderFeature = {
    SenderFeature(term, term(0).isUpper, pos, util.Arrays.hashCode(previousTerm.getBytes).abs, persons.contains(term))
  }

  private def isSender(f: SenderFeature, sender: Option[String], senderPos: Int) = {
    sender.isDefined && sender.get.equalsIgnoreCase(f.term) & senderPos == f.features()(2)
  }
}
