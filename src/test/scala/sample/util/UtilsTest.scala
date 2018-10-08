package sample.util

import org.apache.spark.sql.SparkSession
import org.nd4j.linalg.factory.Nd4j
import org.scalatest.FunSuite

class UtilsTest extends FunSuite {

  val spark: SparkSession = SparkSession
    .builder()
    .appName("sample")
    .master("local[2]")
    .getOrCreate()

  import spark.implicits._

  test("Spark DataFrame is correctly converted to dense matrix Nd4j INDArray") {
    val df = Seq(
      (1.0, 100.0),
      (2.0, 200.0),
      (3.0, 300.0)
    ).toDF("A", "B")
    val output = Utils.dataframeToINDArray(df)
    val expected = Nd4j.create(Array(
      Array(1.0, 100.0),
      Array(2.0, 200.0),
      Array(3.0, 300.0)
    ))

    assert(output.equals(expected))
  }
}
