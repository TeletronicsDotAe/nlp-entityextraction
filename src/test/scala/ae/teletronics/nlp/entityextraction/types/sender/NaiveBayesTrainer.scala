package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 03/06/16.
  */
class NaiveBayesTrainer(lambda: Double = 1.0, modelType: String = "multinomial") extends Trainer {
  override def train(data: RDD[LabeledPoint]) = {
    val model = NaiveBayes.train(data, lambda, modelType)
    new NaiveBayesModel(model)
  }

  override def name(): String = "naiveBayes"

  override def load(sc: SparkContext, fileName: String) = {
    new NaiveBayesModel(org.apache.spark.mllib.classification.NaiveBayesModel.load(sc, fileName))
  }
}
