package io.flatmap.ml.som

import breeze.linalg.{DenseVector, norm}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

trait Metrics {

  def error[T <: Vector](data: RDD[T]): Double

}

trait QuantizationErrorMetrics extends Metrics {

  self: SelfOrganizingMap =>

  override def error[T <: Vector](data: RDD[T]): Double = {
    data.map {
      dataPoint =>
        norm(DenseVector(dataPoint.toArray) - DenseVector(codeBook(winner(dataPoint, codeBook))))
    }.sum() / data.count().toDouble
  }

}