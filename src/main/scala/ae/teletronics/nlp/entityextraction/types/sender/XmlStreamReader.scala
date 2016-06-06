package ae.teletronics.nlp.entityextraction.types.sender

import java.io.InputStream
import java.util

import opennlp.tools.tokenize.SimpleTokenizer
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import ae.teletronics.nlp.entityextraction.types.sender.DoubleUtil.asDouble

/**
  * Created by hhravn on 06/06/16.
  */
class XmlStreamReader(context: SparkContext, tokenizer: SimpleTokenizer) {
  def read(trainingXmlStream: InputStream) : RDD[LabeledPoint] = {
    val messages: Seq[LabeledPoint] = TrainMessage.readMessages(trainingXmlStream)
      .flatMap(t => {
        tokenizer.tokenize("." + t.content)
          .iterator.sliding(2)
          .zipWithIndex
          .map { case (terms, index) => asFeature(terms(1), index, terms(0)) }
          .map(f => LabeledPoint(asDouble(isSender(f, t.s, t.sPos)), f.features))
      })

   context.makeRDD(messages)
  }

  private def asFeature(term: String, pos: Int, previousTerm: String): SenderFeature = {
    SenderFeature(term, term(0).isUpper, pos, util.Arrays.hashCode(previousTerm.getBytes).abs)
  }

  private def isSender(f: SenderFeature, sender: Option[String], senderPos: Int) = {
    sender.isDefined && sender.get.equalsIgnoreCase(f.term) & senderPos == f.features()(1)
  }
}
