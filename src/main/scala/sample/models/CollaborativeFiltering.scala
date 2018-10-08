package sample.models

import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.sql.DataFrame

class CollaborativeFiltering {

  private var als: ALS = _
  private var training: DataFrame = _
  private var test: DataFrame = _
  private var model: ALSModel = _
  private var predictions: DataFrame = _

  def init(iterations: Int = 5, regParam: Double = 0.01): CollaborativeFiltering = {
    als = new ALS()
      .setMaxIter(iterations)
      .setRegParam(regParam)
      .setUserCol("user")
      .setItemCol("movie")
      .setRatingCol("rating")

    this
  }

  def trainTestSplit(ratings: DataFrame, testSize: Double = 0.2, seed: Int = 42): CollaborativeFiltering = {
    val splits = ratings.randomSplit(Array(1 - testSize, testSize), seed = seed)
    training = splits(0)
    test = splits(1)
    this
  }

  def fit(): CollaborativeFiltering = {
    model = als.fit(training)
    this
  }

  def fit(training: DataFrame): CollaborativeFiltering = {
    model = als.fit(training)
    this
  }

  def transform(): CollaborativeFiltering = {
    model.setColdStartStrategy("drop")
    predictions = model.transform(test)
    this
  }

  def transform(test: DataFrame): CollaborativeFiltering = {
    model.setColdStartStrategy("drop")
    predictions = model.transform(test)
    this
  }

  def evaluate(): Unit = {
    val evaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")
    val rmse = evaluator.evaluate(predictions)
    println(s"Root-mean-square error = $rmse")
  }

  def submit(): DataFrame = {
    predictions
  }

}
