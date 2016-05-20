package ae.teletronics.nlp.entityextraction.types.sender

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.util

import ae.teletronics.nlp.entityextraction.types.sender.DoubleUtil.asDouble
import opennlp.tools.tokenize.SimpleTokenizer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint

import scala.xml.{Node, XML}


/**
  * Created by trym on 19-05-2016.
  */
class EngLearner {
  private val tokenizer = SimpleTokenizer.INSTANCE
  val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
  val sc = new SparkContext(conf)

  def learn() = {
    // create input file
    val sparkInputFileName = "target/spark-messages.txt"
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sparkInputFileName)))
    readMessages()
      .foreach(t => {
        tokenizer.tokenize("." + t.content)
          .iterator.sliding(2)
          .zipWithIndex
          .map { case (terms, index) => asFeature(terms(1), index, terms(0)) }
          .map(f => LabeledPoint(asDouble(isSender(f, t.s, t.sPos)), f.features))
          .foreach(lp => writer.write(lp.label + "," + spaceSeparated(lp.features) + "\n"))
      })
    writer.flush()
    writer.close()

    // Split data into training (60%) and test (40%).
    val data = sc.textFile(sparkInputFileName)
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).trim().split(' ').map(_.toDouble)))
    }

    // Split data into training (60%) and test (40%).
    val splits = parsedData.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    val model = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()
  }

  private def spaceSeparated(features: Vector): String =
    features.toArray.foldLeft("") { case (acc, value) => acc + " " + value }

  private def asFeature(term: String, pos: Int, previousTerm: String): SenderFeature = {
    SenderFeature(term, term(0).isUpper, pos, util.Arrays.hashCode(previousTerm.getBytes).abs)
  }

  private def isSender(f: SenderFeature, sender: Option[String], senderPos: Int) = {
    sender.isDefined && sender.get.equalsIgnoreCase(f.term) & senderPos == f.features()(1)
  }

  private def readMessages() = {
    val xml = XML.loadFile("src/main/resources/train/mailinglists.xml")
    (xml \\ "messages" \\ "message")
      .map(fromXml)
  }

  def extract(node: Node, tagName: String) = {
    val name = (node \\ tagName \ "@name").text
    val pos = (node \\ tagName \ "@position").text

    val nameValue = if (name.isEmpty) None else Some(name)
    val posValue = if (pos.isEmpty) -1 else pos.toInt

    (nameValue, posValue)
  }

  private def fromXml(node: Node) = {
    val content = (node \\ "content").text
    val (sender, pos) = extract(node, "sender")
    val (receiver, receiverPos) = extract(node, "receiver")

    TrainMessage(content, receiver, receiverPos, sender, pos)
  }

}
