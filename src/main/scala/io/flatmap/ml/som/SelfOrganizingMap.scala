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

}

trait SelfOrganizingMap extends Serializable { self: NeighborhoodKernel with DecayFunction with Metrics =>

  val width: Int
  val height: Int
  val sigma: Double
  val learningRate: Double

  private[som] lazy val codeBook: CodeBook = DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray)
  private lazy val logger = LoggerFactory.getLogger(getClass)

  def initialize[T <: Vector](data: RDD[T]): SelfOrganizingMap = {
    for {
      (i, j) <- codeBook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, width * height)
    } yield codeBook(i, j) = sample.toArray
    self
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
    implicit var params = Parameters(sigma, learningRate)
    for (i <- 0 until iterations) {
      implicit val bc = sparkSession.sparkContext.broadcast(codeBook)
      val randomizedRDD = data.repartition(partitions)
      val d: Double => Double = decay(_, i, iterations)
      val codeBooks = randomizedRDD.mapPartitions(trainPartition)
      codeBook := codeBooks.reduce(_ + _).map(_.map(_ / partitions.toDouble))
      params = params.copy(d(sigma), d(learningRate), error(randomizedRDD) :: params.errors)
      logger.info(f"iter: $i, sigma: ${params.sigma}%1.4f, learningRate: ${params.learningRate}%1.4f, error: ${params.errors.head}%1.4f")
    }
    (self, params)
  }

}
