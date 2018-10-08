package sample

import org.apache.spark.sql.SparkSession
import sample.models.{CollaborativeFiltering, ContentBased}

object Main {

  def runCollaborativeFiltering(loader: Loader): Unit = {
    val ratings = loader.loadRatings("input/ratings.csv")
    val evalRatings = loader.loadEvalRatings("input/evaluation_ratings.csv")

    val cf = new CollaborativeFiltering()
    cf
      .init()
      .fit(ratings)
      .transform(evalRatings)
      .submit("output/submission.csv")
  }

  def runContentBased(spark: SparkSession, loader: Loader): Unit = {
    val movies = loader.loadMovies("input/movies_metadata.csv")
    val ratings = loader.loadRatings("input/ratings.csv")
    val evalRatings = loader.loadEvalRatings("input/evaluation_ratings.csv")

    val cb = new ContentBased(spark)
    cb
      .preprocess(movies, ratings)
      .init(0.01f, 0.01f, 3)
      .fit()

  }

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName("sample")
      .master("local[2]")
      .getOrCreate()


    val loader = new Loader(spark)
    if (args.length == 0 || args(0).equals("cf")) {
      runCollaborativeFiltering(loader)
    } else if (args(0).equals("cb")) {
      runContentBased(spark, loader)
    }

  }

}
