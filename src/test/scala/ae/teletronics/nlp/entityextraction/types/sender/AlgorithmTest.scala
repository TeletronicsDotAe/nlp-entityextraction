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
  var data: RDD[LabeledPoint] = _
  var sc: SparkContext = _

  @Before
  def setup: Unit = {
    val tokenizer = SimpleTokenizer.INSTANCE
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
    sc = new SparkContext(conf)

    val s = this.getClass.getClassLoader.getResourceAsStream("train/mailinglists.xml")
    val streamReader = new XmlStreamReader(sc, tokenizer)
    data = streamReader.read(s)
  }

  def splitData()  = {
    data.randomSplit(Array(.8, .2))
  }

  @After
  def teardown: Unit = {
    sc.stop()
  }

  @Test
  def naiveBayes() : Unit = {
    println(crossValidate(5, testNaiveBayes))
  }

  @Test
  def decisionTree() : Unit = {
    println(crossValidate(5, testDecisionTree))
  }

  def crossValidate(iterations: Int, inner: RDD[(Double, Double)]) = {
    val r = (1 to iterations)
      .map(i => {
        inner
      })
      .map(f_score)
      .map(r => r
        .filter(p => p._1 == 1)
        .map(p => p._2)
        .first()
      )

    r.sum / iterations
  }

  def testNaiveBayes = {
    val d = splitData

    new NaiveBayesTrainer()
      .train(d(0))
      .test(d(1))
  }

  def testDecisionTree = {
    val d = splitData

    new DecisionTreeTrainer()
      .train(d(0))
      .test(d(1))
  }


  def f_score(r: RDD[(Double, Double)]): RDD[(Double, Double)] ={
    val metrics: BinaryClassificationMetrics = new BinaryClassificationMetrics(r)
    metrics.fMeasureByThreshold()

  }
}
