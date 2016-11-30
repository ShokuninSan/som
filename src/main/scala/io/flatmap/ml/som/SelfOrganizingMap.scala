package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, argmin, norm}
import breeze.numerics.{exp, pow}
import io.flatmap.ml.numerics._
import io.flatmap.ml.som.SelfOrganizingMap.{CodeBook, Neuron}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object SelfOrganizingMap {

  type Neuron = (Int, Int)

  type CodeBook = DenseMatrix[Array[Double]]

  def apply(codeBook: CodeBook, sigma: Double, learningRate: Double) =
    new SelfOrganizingMap(codeBook, sigma, learningRate)

  def apply(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) =
    new SelfOrganizingMap(DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray), sigma, learningRate)

}

class SelfOrganizingMap private (val codeBook: CodeBook, val sigma: Double, val learningRate: Double) extends Serializable {

  private val width = codeBook.cols
  private val height = codeBook.rows

  def initialize(data: RDD[Vector]): Unit =
    for {
      (i, j) <- codeBook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codeBook(i, j) = sample.toArray

  def decay(coefficient: Double, iteration: Int, maxIterations: Int): Double =
    coefficient/(1.0+iteration.toDouble)/maxIterations.toDouble

  def winner(dataPoint: Vector): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](height, width)
    codeBook.foreachPair {
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
