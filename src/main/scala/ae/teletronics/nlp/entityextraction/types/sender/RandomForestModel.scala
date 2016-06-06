package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
class RandomForestModel(model: org.apache.spark.mllib.tree.model.RandomForestModel) extends Model{
  override def predict(data: RDD[Vector]): RDD[Double] = {
    RandomForestModelInternals.predict(model, data)
  }

  override def test(data: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    RandomForestModelInternals.test(model, data)
  }
}

private object RandomForestModelInternals {
  def test(model: org.apache.spark.mllib.tree.model.RandomForestModel, data: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    data.map(p => (model.predict(p.features), p.label))
  }

  def predict(model: org.apache.spark.mllib.tree.model.RandomForestModel, data: RDD[Vector]): RDD[Double] = {
    model.predict(data)
  }
}