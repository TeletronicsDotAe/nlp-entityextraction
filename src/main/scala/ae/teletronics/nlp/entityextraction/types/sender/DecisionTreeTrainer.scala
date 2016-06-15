package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.configuration.{Algo, Strategy}
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
class DecisionTreeTrainer extends Trainer {
  override def train(data: RDD[LabeledPoint]) = {
    val model = DecisionTree.train(data, Strategy.defaultStrategy(Algo.Classification))
    new DecisionTreeModel(model)
  }

  override def name(): String = "decisionTree"

  override def load(sc: SparkContext, fileName: String) =
    new DecisionTreeModel(org.apache.spark.mllib.tree.model.DecisionTreeModel.load(sc, fileName))
}
