package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, norm}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

class SelfOrganizingMap(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) extends Serializable {

  type Neuron = (Int, Int)

  val codebook: DenseMatrix[Array[Double]] = DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray)

  def initialize(data: RDD[Vector]): Unit =
    for {
      index <- codebook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codebook(index._1, index._2) = sample.toArray

  def winner(point: Vector): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](height, width)
    val subtracted = codebook.copy.mapValues(_.zip(point.toArray).map { case (a,b) => a-b })
    subtracted.foreachPair { case ((x, y), v) =>
        activationMap(x, y) = norm(DenseVector(v))
    }
    activationMap.argmin
  }

}
