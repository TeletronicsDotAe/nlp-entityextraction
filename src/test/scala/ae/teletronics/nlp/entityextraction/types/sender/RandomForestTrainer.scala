package ae.teletronics.nlp.entityextraction.types.sender

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.rdd.RDD

/**
  * Created by hhravn on 06/06/16.
  */
class RandomForestTrainer extends Trainer {
  override def train(data: RDD[LabeledPoint]): Model = {
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 10
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "gini"
    val maxDepth = 4
    val maxBins = 32

    val model = RandomForest.trainClassifier(data, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    new RandomForestModel(model)
  }

  override def name(): String = "randomForest"

  override def load(sc: SparkContext, fileName: String) =
    new RandomForestModel(org.apache.spark.mllib.tree.model.RandomForestModel.load(sc, fileName))
}
