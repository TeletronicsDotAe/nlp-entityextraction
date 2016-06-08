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
        trainAlgorithm(trainer, i)
      })
      .map(x => {
        val r = x._1
        val training_data = x._2(0)
        val test_data: RDD[LabeledPoint] = x._2(1)
        val training_data_length = training_data.count()
        val training_data_relevant = training_data.filter(row => row.label == 1).count()
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
    println(trainer.getClass.getSimpleName + ": \n-------------\n" + r.divideBy(xValidationIterations))
    println("----------------------------")
  }

  private def trainAlgorithm(algorithm: Trainer, seed: Long): (RDD[(Double, Double)], Array[RDD[LabeledPoint]]) ={
    val d = data.randomSplit(Array(.7, .3), seed)

    (algorithm
      .train(d(0))
      .test(d(1)), d)
  }

  case class metrics(test_set_length: Double, relevant: Double, returned: Double, positives: Double, false_positives: Double, training_data_length: Double, training_data_relevant: Double){
    def add(other: metrics): metrics = {
      metrics(test_set_length + other.test_set_length, relevant + other.relevant, returned + other.returned, positives + other.positives, false_positives + other.false_positives, training_data_length + other.training_data_length, training_data_relevant + other.training_data_relevant)
    }

    def divideBy(divisor: Double): metrics = {
      metrics(test_set_length / divisor, relevant / divisor, returned / divisor, positives / divisor, false_positives / divisor, training_data_length / divisor, training_data_relevant / divisor)
    }

    override def toString = {
      f"training data length: $training_data_length%1.2f\ntraining data relevant: $training_data_relevant%1.2f\n-------------\ntest set length: $test_set_length%1.2f \nrelevant: $relevant%1.2f\nreturned: $returned%1.2f\ntrue positives: $positives%1.2f\nfalse positives: $false_positives%1.2f"
    }
  }
}
