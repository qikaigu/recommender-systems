package sample.util

import org.apache.spark.sql.DataFrame
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

object Utils {

  def dataframeToINDArray(df: DataFrame): INDArray = {
    val arr: Array[Double] = df.collect.flatMap(_.toSeq).map(_.asInstanceOf[Double])

    val rows = df.count().toInt
    val cols = df.columns.length

    val features = Nd4j.create(arr).reshape(rows, cols)
    features
  }

}
