package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, argmin, norm}
import io.flatmap.ml.som.SelfOrganizingMap.{CodeBook, HyperParameters, Neuron, Weights}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object SelfOrganizingMap {

  type Neuron = (Int, Int)
  type Weights = Array[Double]
  type CodeBook = DenseMatrix[Array[Double]]
  case class HyperParameters(sigma: Double, learningRate: Double)

  def apply(codeBook: CodeBook, sigma: Double, learningRate: Double) = {
    new SelfOrganizingMap(codeBook, sigma, learningRate)
  }

  def apply(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) = {
    new SelfOrganizingMap(DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray), sigma, learningRate)
  }

}

class SelfOrganizingMap private (val codeBook: CodeBook, val sigma: Double, val learningRate: Double) extends Serializable with GaussianNeighborboodKernel with CustomDecay with QuantizationErrorMetrics {

  private val width = codeBook.cols
  private val height = codeBook.rows

  def initialize[T <: Vector](data: RDD[T]): SelfOrganizingMap = {
    val codeBook = this.codeBook.copy
    for {
      (i, j) <- codeBook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codeBook(i, j) = sample.toArray
    new SelfOrganizingMap(codeBook, sigma, learningRate)
  }

  def winner(dataPoint: Vector, codeCook: CodeBook): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](height, width)
    codeBook.foreachPair {
      case ((i, j), w) =>
        activationMap(i, j) = norm(DenseVector(dataPoint.toArray) - DenseVector(w))
    }
    argmin(activationMap)
  }

  private[som] def withUpdatedWeights(block: (Neuron, Weights) => Unit)(implicit codeBook: CodeBook, hp: HyperParameters, dataPoint: Vector): ((Int, Int), Double) => Unit = {
    case ((i, j), h) =>
      block(
        (i, j),
        (DenseVector(codeBook(i, j))
          + hp.learningRate
          * h
          * (DenseVector(dataPoint.toArray)
          - DenseVector(codeBook(i, j)))).toArray)
  }

  private[som] def trainPartition(dataPartition: Iterator[Vector])(implicit broadcast: Broadcast[CodeBook], hp: HyperParameters): Iterator[CodeBook] = {
    implicit val localCodeBook = broadcast.value
    dataPartition foreach { implicit dataPoint =>
      neighborhood(winner(dataPoint, localCodeBook)) foreachPair {
        withUpdatedWeights {
          case ((i, j), weights) =>
            localCodeBook(i, j) = weights
        }
      }
    }
    Array(localCodeBook).iterator
  }

  def train[T <: Vector](data: RDD[T], iterations: Int, partitions: Int = 12)(implicit sparkSession: SparkSession): SelfOrganizingMap = {
    var codeBook = this.codeBook.copy
    implicit var hp = HyperParameters(this.sigma, this.learningRate)
    for (i <- 0 until iterations) {
      val d: Double => Double = decay(_, i, iterations)
      hp = HyperParameters(d(this.sigma), d(this.learningRate))
      implicit val bc = sparkSession.sparkContext.broadcast(codeBook)
      val randomizedRDD = data.repartition(partitions)
      print(s"iter: $i, sigma: ${hp.sigma}, learningRate: ${hp.learningRate}, error: ${error(randomizedRDD)}")
      val resultCodeBook = randomizedRDD.mapPartitions(trainPartition)
      val newCodeBook = resultCodeBook.reduce(_ + _)
      newCodeBook.map(v => v.map(_ / partitions.toDouble))
      codeBook = newCodeBook
    }
    new SelfOrganizingMap(codeBook, hp.sigma, hp.learningRate)
  }

}
