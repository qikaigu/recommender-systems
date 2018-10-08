package sample.models

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoderEstimator, StringIndexer}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.ops.transforms.Transforms._
import org.nd4s.Evidences.float
import org.nd4s.Implicits._
import sample.Rating
import sample.util.Utils

class ContentBased(spark: SparkSession) {

  import spark.implicits._

  private var alpha: Float = _
  private var lambda: Float = _
  private var iterations: Int = _
  private var movies: DataFrame = _
  private var movieIdMapping: collection.Map[Int, Int] = _
  private var movieFeatures: INDArray = _
  private var computedThetas: INDArray = _
  private var userIdMapping: Map[Int, Int] = _
  private var ratingsWithIndices: Array[Rating] = _
  private var training: DataFrame = _
  private var test: DataFrame = _
  private var predictions: DataFrame = _

  def preprocess(movies: DataFrame, ratings: DataFrame): ContentBased = {
    this.movies = movies
    movieIdMapping = movies.select("movieId").map(row => row.getInt(0)).rdd
      .zipWithIndex()
      .map { case (movieId, index) => (movieId, index.toInt) }
      .collectAsMap()

    val indexer = new StringIndexer()
      .setInputCol("original_language")
      .setOutputCol("original_language_index")

    val encoder = new OneHotEncoderEstimator()
      .setInputCols(Array("original_language_index"))
      .setOutputCols(Array("original_language_onehot"))

    val pipeline = new Pipeline()
      .setStages(Array(indexer, encoder))

    val model = pipeline.fit(this.movies)
    this.movies = model.transform(this.movies)

    val validMovieIds = movieIdMapping.keySet.toList
    val validRatings = ratings.filter($"movieId" isin (validMovieIds: _*))

    ratingsWithIndices = validRatings.map {
      case Row(user: Int, movie: Int, rating: Float, timestamp: Int) =>
        Rating(user, movieIdMapping(movie), rating, timestamp)
    }.collect.toList.toArray

    movieFeatures = Utils.dataframeToINDArray(movies.select("popularity", "vote_average"))

    this
  }

  def init(alpha: Float, lambda: Float, iterations: Int): ContentBased = {
    this.alpha = alpha
    this.lambda = lambda
    this.iterations = iterations
    this
  }

  def trainTestSplit(ratings: DataFrame, testSize: Double = 0.2, seed: Int = 42): ContentBased = {
    val splits = ratings.randomSplit(Array(1 - testSize, testSize), seed = seed)
    training = splits(0)
    test = splits(1)
    this
  }

  def fit(): ContentBased = {
    val nFeatures: Int = movieFeatures.shape()(0)
    val nMovies: Int = movieFeatures.shape()(1)
    val userIds: Array[Int] = ratingsWithIndices.map(_.user).distinct

    userIdMapping = userIds.zipWithIndex.toMap

    val bias = Nd4j.ones(1, nMovies)
    val features = Nd4j.concat(0, bias, movieFeatures)
    val thetas: INDArray = Nd4j.zeros(1 + nFeatures, userIds.length)

    computedThetas = computeGradient(features, ratingsWithIndices, thetas, alpha, lambda, iterations)
    this
  }

  def transform(): ContentBased = {

    this
  }

  def evaluate(): Unit = {

  }

  private def computeCost(movieFeatures: INDArray, ratings: Array[Rating], thetas: INDArray): Float = {
    var cost: Float = 0
    for (r <- ratings) {
      val dotProduct: INDArray = thetas.getColumn(userIdMapping(r.user)).T.dot(movieFeatures.getColumn(r.movie))
      val y = Nd4j.valueArrayOf(Array(1, 1), r.rating.toDouble)
      cost += dotProduct.squaredDistance(y).toFloat / 2
    }

    val regularization = pow(thetas.get(NDArrayIndex.interval(1, thetas.shape()(0))), 2).sumNumber().floatValue() // submatrix [1:][:]
    cost += regularization
    cost
  }

  private def computeGradient(movieFeatures: INDArray, ratings: Array[Rating], thetas: INDArray, alpha: Float, lambda: Float, iterations: Int): INDArray = {
    val computedThetas = (0 to iterations).foldLeft(thetas)({
      case (thetas, i) =>
        for (r <- ratings) {
          val dotProduct: INDArray = thetas.getColumn(userIdMapping(r.user)).T.dot(movieFeatures.getColumn(r.movie))
          val y = Nd4j.valueArrayOf(Array(1, 1), r.rating.toDouble)
          val diff = dotProduct.sub(y)
          val update = movieFeatures.getColumn(r.movie).dot(diff)
          thetas.putColumn(userIdMapping(r.user), thetas.getColumn(userIdMapping(r.user)) - update.mul(alpha))
        }
        val regularization = thetas.mul(alpha * lambda)
        regularization.putRow(0, regularization.getRow(0).mul(0))
        val updatedThetas = thetas.sub(regularization)

        println(s"Iteration $i cost: ${computeCost(movieFeatures, ratings, thetas)}")
        updatedThetas
    })
    computedThetas
  }


}
