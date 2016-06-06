package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
class NaiveBayesModel(val model: org.apache.spark.mllib.classification.NaiveBayesModel) extends Model{
  override def predict(data: RDD[org.apache.spark.mllib.linalg.Vector]): RDD[Double] = {
    NaiveBayesModelInternals.predict(model, data)
  }

  override def test(data: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    NaiveBayesModelInternals.test(model, data)
  }
}

private object NaiveBayesModelInternals {
  def predict(model: org.apache.spark.mllib.classification.NaiveBayesModel, data: RDD[org.apache.spark.mllib.linalg.Vector]): RDD[Double] = {
    model.predict(data)
  }

  def test(model: org.apache.spark.mllib.classification.NaiveBayesModel, data: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    data.map(p => (model.predict(p.features), p.label))
  }
}
