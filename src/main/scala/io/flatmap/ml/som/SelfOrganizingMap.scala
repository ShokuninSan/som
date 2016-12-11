package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, argmin, norm}
import io.flatmap.ml.som.SelfOrganizingMap.{CodeBook, Neuron, Parameters, Weights}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object SelfOrganizingMap {

  type Neuron = (Int, Int)
  type Weights = Array[Double]
  type CodeBook = DenseMatrix[Array[Double]]

  case class Parameters(sigma: Double, learningRate: Double, errors: List[Double] = Nil)

  def apply(codeBook: CodeBook, sigma: Double, learningRate: Double) = {
    new SelfOrganizingMap(codeBook, sigma, learningRate)
  }

  def apply(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) = {
    new SelfOrganizingMap(DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray), sigma, learningRate)
  }

}

class SelfOrganizingMap private (var codeBook: CodeBook, val sigma: Double, val learningRate: Double) extends Serializable with GaussianNeighborboodKernel with CustomDecay with QuantizationErrorMetrics {

  private val width = codeBook.cols
  private val height = codeBook.rows

  private val logger = LoggerFactory.getLogger(classOf[SelfOrganizingMap])

  def initialize[T <: Vector](data: RDD[T]): SelfOrganizingMap = {
    val codeBook = this.codeBook.copy
    for {
      (i, j) <- codeBook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codeBook(i, j) = sample.toArray
    new SelfOrganizingMap(codeBook, sigma, learningRate)
  }

  private[som] def winner(dataPoint: Vector, codeCook: CodeBook): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](height, width)
    codeBook.foreachPair {
      case ((i, j), w) =>
        activationMap(i, j) = norm(DenseVector(dataPoint.toArray) - DenseVector(w))
    }
    argmin(activationMap)
  }

  private[som] def withUpdatedWeights(block: (Neuron, Weights) => Unit)(implicit codeBook: CodeBook, p: Parameters, dataPoint: Vector): ((Int, Int), Double) => Unit = {
    case ((i, j), h) =>
      block(
        (i, j),
        (DenseVector(codeBook(i, j))
          + p.learningRate
          * h
          * (DenseVector(dataPoint.toArray)
          - DenseVector(codeBook(i, j)))).toArray)
  }

  private[som] def trainPartition(dataPartition: Iterator[Vector])(implicit broadcast: Broadcast[CodeBook], p: Parameters): Iterator[CodeBook] = {
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

  def train[T <: Vector](data: RDD[T], iterations: Int, partitions: Int = 12)(implicit sparkSession: SparkSession): (SelfOrganizingMap, Parameters) = {
    implicit var params = Parameters(this.sigma, this.learningRate)
    for (i <- 0 until iterations) {
      implicit val bc = sparkSession.sparkContext.broadcast(this.codeBook)
      val randomizedRDD = data.repartition(partitions)
      val d: Double => Double = decay(_, i, iterations)
      val codeBooks = randomizedRDD.mapPartitions(trainPartition)
      this.codeBook = codeBooks.reduce(_ + _).map(_.map(_ / partitions.toDouble))
      params = params.copy(d(this.sigma), d(this.learningRate), error(randomizedRDD) :: params.errors)
      logger.info(f"iter: $i, sigma: ${params.sigma}%1.4f, learningRate: ${params.learningRate}%1.4f, error: ${params.errors.head}%1.4f")
    }
    (new SelfOrganizingMap(this.codeBook.copy, this.sigma, this.learningRate), params)
  }

}
