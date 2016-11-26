package io.flatmap.ml.som

import breeze.linalg.DenseMatrix
import org.apache.spark.rdd.RDD

class SelfOrganizingMap(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) extends Serializable {

  val codebook: DenseMatrix[Double] = DenseMatrix.zeros[Double](height, width)

  def initialize(data: RDD[Double]): Unit =
    for {
      index <- codebook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codebook(index._1, index._2) = sample

}
