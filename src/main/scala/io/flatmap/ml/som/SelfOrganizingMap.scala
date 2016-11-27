package io.flatmap.ml.som

import breeze.linalg.DenseMatrix
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vector

class SelfOrganizingMap(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) extends Serializable {

  type Neuron = (Int, Int)

  val codebook: DenseMatrix[Array[Double]] = DenseMatrix.zeros[Array[Double]](height, width)

  def initialize(data: RDD[Vector]): Unit =
    for {
      index <- codebook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codebook(index._1, index._2) = sample.toArray

}
