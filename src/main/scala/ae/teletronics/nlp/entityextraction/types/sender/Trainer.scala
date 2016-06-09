package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
trait Trainer {
  def train(data: RDD[LabeledPoint]): Model
  def name(): String
  def load(sc: SparkContext, fileName: String): Model
}

trait Model {
  def predict(data: RDD[org.apache.spark.mllib.linalg.Vector]): RDD[Double]
  def test(data: RDD[LabeledPoint]): RDD[(Double, Double)]
  def save(sc: SparkContext, fileName: String)
}
