package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, argmin, norm}
import breeze.numerics.{exp, pow}
import io.flatmap.ml.numerics._
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

  def winner(dataPoint: Vector): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](height, width)
    codebook
      .copy
      .foreachPair {
        case ((i, j), w) =>
          activationMap(i, j) = norm(DenseVector(dataPoint.toArray) - DenseVector(w))
      }
    argmin(activationMap)
  }

  def gaussian(winner: Neuron): DenseMatrix[Double] = {
    def gaussianVector(size: Int, mean: Int): DenseVector[Double] = {
      val neighbors = DenseVector.range(0, size) - mean
      val numerator = pow(neighbors, 2).map(_.toDouble)
      val denominator = 2*math.Pi*sigma*sigma
      exp(-(numerator /:/ denominator))
    }
    // horizontal gaussian distribution of neighborhood coefficients
    val gaussianX = gaussianVector(width, winner._1)
    // vertical gaussian distribution of neighborhood coefficients
    val gaussianY = gaussianVector(height, winner._2)
    // return 2 dimensional gaussian dist. surface by creating the outer product
    outer(gaussianX, gaussianY)
  }

}
