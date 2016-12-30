package util

import io.flatmap.ml.som.Metrics
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

trait FakeMetrics extends Metrics {

  override def error[T <: Vector](data: RDD[T]): Double = ???

}
