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
  val xValidationIterations = 10
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

  @After
  def teardown: Unit = {
    sc.stop()
  }

  @Test
  def randomForest() : Unit = {
    crossValidate(new RandomForestTrainer)
  }

  @Test
  def naiveBayes() : Unit = {
    crossValidate(new NaiveBayesTrainer)
  }

  @Test
  def decisionTree() : Unit = {
    crossValidate(new DecisionTreeTrainer)
  }

  private def crossValidate(trainer: Trainer): Unit ={
    val r = (1 to xValidationIterations)
      .map(i => {
        trainAlgorithm(trainer, xValidationIterations)
      })
      .map(f_score)
      .filter(r => r.count() > 0)
      //Note: Really need to revisit performance calculations...
      .map(r =>
      r
        .filter(p => {
          p._1 == 1
        })
        .map(p => p._2)
    )
      .map(score => {
        //Note: Sometimes no scores are returned. Must be handled in some way..
        if(score.isEmpty()) 0 else score.first()
      })

    println(trainer.getClass.getSimpleName + ": " + r.sum / xValidationIterations)
  }

  private def trainAlgorithm(algorithm: Trainer, seed: Long): RDD[(Double, Double)] ={
    val d = data.randomSplit(Array(.7, .3), seed)

    algorithm
      .train(d(0))
      .test(d(1))
  }


  private def f_score(r: RDD[(Double, Double)]): RDD[(Double, Double)] ={
    val metrics: BinaryClassificationMetrics = new BinaryClassificationMetrics(r)
    metrics.fMeasureByThreshold()
  }
}
