package io.flatmap.ml.som

import breeze.linalg.{DenseVector, norm}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

trait Metrics {

  def error[T <: Vector](data: RDD[T]): Double

}

/** Quantization error metrics
  *
  * This trait is for composition with a [[io.flatmap.ml.som.SelfOrganizingMap]]:
  *
  * {{{
  * val SOM = new SelfOrganizingMap with CustomDecay with GaussianNeighborboodKernel with QuantizationErrorMetrics {
  *   override val shape: Shape = (24, 24)
  *   override val learningRate: Double = 0.3
  *   override val sigma: Double = 0.5
  * }
  * }}}
  */
trait QuantizationErrorMetrics extends Metrics { self: SelfOrganizingMap =>

  /** Computes the error of the [[io.flatmap.ml.som.SelfOrganizingMap]]
    *
    * This method computes the average of the Euclidean distances between each datapoint and its BMU.
    *
    * @tparam T a subtype of [[org.apache.spark.mllib.linalg.Vector]]
    * @param data an [[org.apache.spark.rdd.RDD]] with datapoints
    * @return the error of the [[io.flatmap.ml.som.SelfOrganizingMap]]
    */
  override def error[T <: Vector](data: RDD[T]): Double = {
    data.map {
      dataPoint =>
        norm(DenseVector(dataPoint.toArray) - DenseVector(codeBook(winner(dataPoint, codeBook))))
    }.sum() / data.count().toDouble
  }

}