package ae.teletronics.nlp.entityextraction.types.sender

import opennlp.tools.tokenize.SimpleTokenizer
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.{After, Before, Test}

/**
  * Created by trym on 19-05-2016.
  */
class AlgorithmTest {
  var testData: RDD[LabeledPoint] = _
  var data: RDD[LabeledPoint] = _
  var sc: SparkContext = _
  val thresholds = Array(1, .8, .6, .4, .2)

  @Before
  def setup: Unit = {
    val tokenizer = SimpleTokenizer.INSTANCE
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
    sc = new SparkContext(conf)

    val s = this.getClass.getClassLoader.getResourceAsStream("train/mailinglists.xml")
    val streamReader = new XmlStreamReader(sc, tokenizer)
    val d = streamReader.read(s).randomSplit(Array(.6,.4), seed = 11L)
    data = d(0)
    testData = d(1)
  }

  @After
  def teardown: Unit = {
    sc.stop()
  }

  @Test
  def testNaiveBayesLearner() : Unit = {
    val r = new NaiveBayesTrainer()
      .train(data)
      .test(testData)

    printPerformance(r)
  }

  @Test
  def testDecisionTree() : Unit = {
    val r = new DecisionTreeTrainer()
      .train(data)
      .test(testData)

    printPerformance(r)
  }


  def printPerformance(r: RDD[(Double, Double)]): Unit ={
    val metrics: BinaryClassificationMetrics = new BinaryClassificationMetrics(r)
    //val precision: RDD[(Double, Double)] = metrics.precisionByThreshold()
    //val recall: RDD[(Double, Double)] = metrics.recallByThreshold()
    val f1: RDD[(Double, Double)] = metrics.fMeasureByThreshold()

    f1.foreach(f => println(s"threshold: ${f._1} , score: ${f._2} "))
  }
}
