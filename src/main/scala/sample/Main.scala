package sample

import org.apache.spark.sql.SparkSession
import sample.models.CollaborativeFiltering

object Main {

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName("sample")
      .master("local[2]")
      .getOrCreate()


    val loader = new Loader(spark)
    val ratings = loader.loadRatings("input/ratings.csv")
    val evalRatings = loader.loadEvalRatings("input/evaluation_ratings.csv")

    val cf = new CollaborativeFiltering()
    cf
      .init()
      .fit(ratings)
      .transform(evalRatings)
      .submit("output/submission.csv")
  }

}
