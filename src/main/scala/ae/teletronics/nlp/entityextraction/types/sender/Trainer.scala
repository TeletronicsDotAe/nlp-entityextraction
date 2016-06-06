package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
trait Trainer {
  def train(data: RDD[LabeledPoint]) : Model
}

trait Model {
  def predict(data: RDD[LabeledPoint]) : RDD[Double]
  def test(data: RDD[LabeledPoint]) : RDD[(Double, Double)]
}
