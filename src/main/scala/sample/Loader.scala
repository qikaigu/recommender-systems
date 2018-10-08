package sample

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{from_json, monotonically_increasing_id}
import org.apache.spark.sql.types._

class Loader(spark: SparkSession) {

  import spark.implicits._

  def loadMovies(path: String): DataFrame = {
    val movie_schema = StructType(Seq(
      StructField("adult", BooleanType, true),
      StructField("belongs_to_collection", StringType, true),
      StructField("budget", DoubleType, true),
      StructField("genres", StringType, true),
      StructField("homepage", StringType, true),
      StructField("id", IntegerType, true),
      StructField("imdb_id", StringType, true),
      StructField("original_language", StringType, true),
      StructField("original_title", StringType, true),
      StructField("overview", StringType, true),
      StructField("popularity", DoubleType, true),
      StructField("poster_path", StringType, true),
      StructField("production_companies", StringType, true),
      StructField("production_countries", StringType, true),
      StructField("release_date", StringType, true),
      StructField("revenue", DoubleType, true),
      StructField("runtime", DoubleType, true),
      StructField("spoken_languages", StringType, true),
      StructField("status", StringType, true),
      StructField("tagline", StringType, true),
      StructField("title", StringType, true),
      StructField("video", BooleanType, true),
      StructField("vote_average", DoubleType, true),
      StructField("vote_count", IntegerType, true),
      StructField("_corrupt_record", StringType, true)
    ))

    val belongs_to_collection_schema = StructType(Seq(
      StructField("id", IntegerType, true),
      StructField("name", StringType, true),
      StructField("poster_path", StringType, true),
      StructField("backdrop_path", StringType, true)
    ))
    val genres_schema = ArrayType(StructType(Array(
      StructField("id", IntegerType, true),
      StructField("name", StringType, true)
    )), true)
    val production_companies_schema = ArrayType(StructType(Array(
      StructField("id", IntegerType, true),
      StructField("name", StringType, true)
    )), true)
    val production_countries_schema = ArrayType(StructType(Array(
      StructField("iso_3166_1", StringType, true),
      StructField("name", StringType, true)
    )), true)
    val spoken_languages_schema = ArrayType(StructType(Array(
      StructField("iso_639_1", StringType, true),
      StructField("name", StringType, true)
    )), true)


    val movies: DataFrame = spark.read
      .format("csv")
      .option("header", "true")
      .option("delimiter", ",")
      .option("quote", "\"")
      .option("escape", "\"")
      .option("mode", "DROPMALFORMED")
      .schema(movie_schema)
      .load(path)
      .withColumnRenamed("id", "movieId")
      .withColumn("id", monotonically_increasing_id)
      .withColumn("belongs_to_collection", from_json($"belongs_to_collection", belongs_to_collection_schema).getField("id"))
      .withColumn("genres", from_json($"genres", genres_schema).getField("id"))
      .withColumn("production_companies", from_json($"production_companies", production_companies_schema).getField("id"))
      .withColumn("production_countries", from_json($"production_countries", production_countries_schema).getField("iso_3166_1"))
      .withColumn("spoken_languages", from_json($"spoken_languages", spoken_languages_schema).getField("iso_639_1"))
      .drop("imdb_id", "poster_path")

    movies
  }
}
