package ae.teletronics.nlp.entityextraction.types.sender

import opennlp.tools.tokenize.SimpleTokenizer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.{After, Before, Test}

/**
  * Created by trym on 19-05-2016.
  */
class AlgorithmTest {
  val xValidationIterations = 3
  var data: RDD[LabeledPoint] = _
  var sc: SparkContext = _

  var loggerLevel:org.apache.log4j.Level = _

  @Before
  def setup: Unit = {
    loggerLevel = Logger.getRootLogger.getLevel
    Logger.getRootLogger().setLevel(Level.OFF);
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val tokenizer = SimpleTokenizer.INSTANCE
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
    sc = new SparkContext(conf)

    val s = this.getClass.getClassLoader.getResourceAsStream("train/mailinglists.xml")
    val streamReader = new XmlStreamReader(sc, tokenizer)
    data = streamReader.read(s)
  }

  @After
  def teardown: Unit = {
    Logger.getRootLogger().setLevel(loggerLevel);
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
      .map(x => {
        val r = x._1
        val t = x._2
        val training_data_length = t.count()
        val training_data_relevant = t.filter(row => row.label == 1).count()
        val relevant = r.filter(row => row._2 == 1)
        val returned = r.filter(row => row._1 == 1)
        val positives = relevant.intersection(returned)
        val false_positives = returned.subtract(relevant)
        metrics(r.count(), relevant.count(), returned.count(), positives.count(), false_positives.count(), training_data_length, training_data_relevant)
      })
      .reduce((metrics, sum) => {
        sum.add(metrics)
      })

    println("----------------------------")
    println(trainer.getClass.getSimpleName + ": \n-------------\n" + r)
    println("----------------------------")
  }

  private def trainAlgorithm(algorithm: Trainer, seed: Long): (RDD[(Double, Double)], RDD[LabeledPoint]) ={
    val d = data.randomSplit(Array(.7, .3), seed + 2)

    (algorithm
      .train(d(0))
      .test(d(1)), d(0))
  }

  case class metrics(test_set_length: Long, relevant: Long, returned: Long, positives: Long, false_positives: Long, training_data_length: Long, training_data_relevant: Long){
    def add(other: metrics): metrics = {
      return metrics(test_set_length + other.test_set_length, relevant + other.relevant, returned + other.returned, positives + other.positives, false_positives + other.false_positives, training_data_length + other.training_data_length, training_data_relevant + other.training_data_relevant)
    }

    override def toString = {
      s"training data length: ${training_data_length}\ntraining data relevant: ${training_data_relevant}\n-------------\ntest set length: ${test_set_length}\nrelevant: ${relevant}\nreturned: ${returned}\ntrue positives: ${positives}\nfalse positives: ${false_positives}"
    }
  }
}
