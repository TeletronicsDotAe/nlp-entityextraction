package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 03/06/16.
  */
class NaiveBayesTrainer(lambda: Double = 1.0, modelType: String = "multinomial") extends Trainer{
  override def train(data: RDD[LabeledPoint]) = {
    val model: org.apache.spark.mllib.classification.NaiveBayesModel = NaiveBayes.train(data, lambda = lambda, modelType = modelType)
    new NaiveBayesModel(model)
  }
}
