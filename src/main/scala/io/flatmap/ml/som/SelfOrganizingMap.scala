package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, argmin, norm}
import io.flatmap.ml.som.SelfOrganizingMap.{CodeBook, Neuron}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object SelfOrganizingMap {

  type Neuron = (Int, Int)
  type CodeBook = DenseMatrix[Array[Double]]

  def apply(codeBook: CodeBook, sigma: Double, learningRate: Double) = {
    new SelfOrganizingMap(codeBook, sigma, learningRate)
  }

  def apply(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) = {
    new SelfOrganizingMap(DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray), sigma, learningRate)
  }

}

class SelfOrganizingMap private (val codeBook: CodeBook, val sigma: Double, val learningRate: Double) extends Serializable with GaussianNeighborboodKernel with CustomDecay {

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

  def winner(dataPoint: Vector): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](height, width)
    codeBook.foreachPair {
      case ((i, j), w) =>
        activationMap(i, j) = norm(DenseVector(dataPoint.toArray) - DenseVector(w))
    }
    argmin(activationMap)
  }

  def train[T <: Vector](data: RDD[T], iterations: Int, partitions: Int = 12)(implicit sparkSession: SparkSession): SelfOrganizingMap = {
    var codeBook = this.codeBook.copy
    for (iteration <- 0 until iterations) {
      val sigma = decay(this.sigma, iteration, iterations)
      val learningRate = decay(this.learningRate, iteration, iterations)
      val broadcastedCodeBook = sparkSession.sparkContext.broadcast(codeBook)
      val randomizedRDD = data.repartition(partitions)
      print(s"iter: $iteration, sigma: $sigma, learningRate: $learningRate")
      def trainPartition = (dataPartition: Iterator[Vector]) => {
        val localCodeBook = broadcastedCodeBook.value
        dataPartition.foreach { dataPoint =>
          val bestMatchingUnit = winner(dataPoint)
          neighborhood(bestMatchingUnit).foreachPair {
            case ((i, j), h) =>
              localCodeBook(i, j) =
                (DenseVector(localCodeBook(i, j))
                  + learningRate
                  * h
                  * (DenseVector(dataPoint.toArray)
                    - DenseVector(localCodeBook(i, j)))).toArray
          }
        }
        Array(localCodeBook).iterator
      }
      val resultCodeBook = randomizedRDD.mapPartitions(trainPartition)
      val newCodeBook = resultCodeBook.reduce(_ + _)
      newCodeBook.map(v => v.map(_ / partitions.toDouble))
      codeBook = newCodeBook
    }
    new SelfOrganizingMap(codeBook, sigma, learningRate)
  }

}
