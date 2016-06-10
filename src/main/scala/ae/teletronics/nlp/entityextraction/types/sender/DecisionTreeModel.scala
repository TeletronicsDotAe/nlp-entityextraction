package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
class DecisionTreeModel(val model: org.apache.spark.mllib.tree.model.DecisionTreeModel) extends Model{
  override def test(data: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    DecisionTreeModelInternals.test(model, data)
  }

  override def predict(data: RDD[org.apache.spark.mllib.linalg.Vector]): RDD[Double] = {
    DecisionTreeModelInternals.predict(model, data)
  }

  override def save(sc: SparkContext, fileName: String): Unit = model.save(sc, fileName)

}

object DecisionTreeModelInternals {
  def predict(model: org.apache.spark.mllib.tree.model.DecisionTreeModel, data: RDD[org.apache.spark.mllib.linalg.Vector]): RDD[Double] = {
    model.predict(data)
  }

  def test(model: org.apache.spark.mllib.tree.model.DecisionTreeModel, data: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    data.map(p => (model.predict(p.features), p.label))
  }
}
