package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector, argmin, norm}
import io.flatmap.ml.som.SelfOrganizingMap._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

/** Companion object for trait [[io.flatmap.ml.som.SelfOrganizingMap]]
  *
  * This object holds types related to the [[io.flatmap.ml.som.SelfOrganizingMap]] trait.
  */
object SelfOrganizingMap {

  type Neuron = (Int, Int)
  type Weights = Array[Double]
  type CodeBook = DenseMatrix[Array[Double]]

  case class Shape(width: Int, height: Int)

  object Shape {

    implicit def tupleToShape(t: (Int, Int)): Shape = Shape(t._1, t._2)

  }

  case class Parameters(sigma: Double, learningRate: Double, errors: List[Double] = Nil)

}

/** Self Organizing Map
  *
  * This is a fairly simple Self Organizing Map for Scala and Apache Spark.
  *
  * To use it, make sure you have an implicit `SparkSession` and your data RDD ready.
  *
  * {{{
  * implicit val sparkSession = ???
  * val data: RDD[Vector] = ???
  * }}}
  *
  * To instantiate a [[io.flatmap.ml.som.SelfOrganizingMap]] instance you need to mix-in predefined or custom
  * implementations of [[io.flatmap.ml.som.DecayFunction, [[io.flatmap.ml.som.NeighborhoodKernel]] and
  * [[io.flatmap.ml.som.Metrics]]:
  *
  * {{{
  * val SOM = new SelfOrganizingMap with CustomDecay with GaussianNeighborboodKernel with QuantizationErrorMetrics {
  *   override val shape: Shape = (24, 24)
  *   override val learningRate: Double = 0.3
  *   override val sigma: Double = 0.5
  * }
  * }}}
  *
  * Initialization and training:
  *
  * {{{
  * val (som, params) = SOM.initialize(data).train(data, 20)
  * }}}
  */
trait SelfOrganizingMap extends Serializable { self: NeighborhoodKernel with DecayFunction with Metrics =>

  val shape: Shape
  val sigma: Double
  val learningRate: Double

  private[som] lazy val codeBook = DenseMatrix.fill[Array[Double]](shape.height, shape.width)(Array.emptyDoubleArray)
  private lazy val logger = LoggerFactory.getLogger(getClass)

  /** Randomly initializes the codebook of the SOM
    *
    * This method takes a fixed-size, randomly sampled subset of the RDD and assigns those datapoints to the codebook.
    *
    * @param data an [[org.apache.spark.rdd.RDD]] with data points
    * @tparam T a subtype of [[org.apache.spark.mllib.linalg.Vector]]
    * @return the current [[io.flatmap.ml.som.SelfOrganizingMap]] instance
    */
  def initialize[T <: Vector](data: RDD[T]): SelfOrganizingMap = {
    for {
      (i, j) <- codeBook.keysIterator.toArray
      sample <- data.takeSample(withReplacement = true, shape.width * shape.height)
    } yield codeBook(i, j) = sample.toArray
    self
  }

  /** Computes and returns the Best Matching Unit (BMU) Neuron
    *
    * This method uses Euclidean distance to compute the distance between the data point and the weights of the
    * Neurons (codebook vectors).
    *
    * @see https://en.wikipedia.org/wiki/Euclidean_distance#definition
    * @param dataPoint a [[org.apache.spark.mllib.linalg.Vector]] representing the input
    * @param codeBook a [[io.flatmap.ml.som.SelfOrganizingMap.CodeBook]] with weight vectors
    * @return the Best matching Unit (BMU) Neuron
    */
  private[som] def winner(dataPoint: Vector, codeCook: CodeBook): Neuron = {
    val activationMap = DenseMatrix.zeros[Double](shape.height, shape.width)
    codeBook.foreachPair {
      case ((i, j), w) =>
        activationMap(i, j) = norm(DenseVector(dataPoint.toArray) - DenseVector(w))
    }
    argmin(activationMap)
  }

  /** Provides a function for updating codebook vectors
    *
    * This method is used to update a codebook according to given Neurons (`(i, j)`) and their neigborhood coefficients
    * denoted as `h`.
    *
    * @param func a higher-order function to be called with updated weights
    * @param codeBook a [[io.flatmap.ml.som.SelfOrganizingMap.CodeBook]] with current weight vectors
    * @param p a [[io.flatmap.ml.som.SelfOrganizingMap.Parameters]] instance with actual learning rate
    * @param dataPoint a [[org.apache.spark.mllib.linalg.Vector]] representing the actual data point
    * @return a function for updating codebook vectors
    */
  private[som] def withUpdatedWeights(func: (Neuron, Weights) => Unit)(implicit codeBook: CodeBook, p: Parameters, dataPoint: Vector): ((Int, Int), Double) => Unit = {
    case ((i, j), h) =>
      func(
        (i, j),
        (DenseVector(codeBook(i, j))
          + p.learningRate
          * h
          * (DenseVector(dataPoint.toArray)
          - DenseVector(codeBook(i, j)))).toArray)
  }

  /** Trains/updates a partition of a dataset/RDD
    *
    * @param dataPartition a partition of a dataset
    * @param broadcast a broadcasted [[io.flatmap.ml.som.SelfOrganizingMap.CodeBook]] with actual weights
    * @param p a [[io.flatmap.ml.som.SelfOrganizingMap.Parameters]] instance with actual learning rate
    * @return a [[scala.Iterator]] which contains an updated/trained [[io.flatmap.ml.som.SelfOrganizingMap.CodeBook]]
    */
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

  /** Trains a [[io.flatmap.ml.som.SelfOrganizingMap]]
    *
    * This method broadcasts the actual codebook, repartitions the entire dataset in each epoch and trains those
    * partitions in parallel using actual [[io.flatmap.ml.som.SelfOrganizingMap.Parameters]]. At the end of each
    * iteration the codebooks of all partitions are averaged. The result is stored in the local codebook.
    *
    * @tparam T a subtype of [[org.apache.spark.mllib.linalg.Vector]]
    * @param data an [[org.apache.spark.rdd.RDD]] with data points
    * @param iterations number of trainting iterations/epochs
    * @param partitions number of partitions for the training data
    * @param sparkSession instance of [[org.apache.spark.sql.SparkSession]]
    * @return a tuple with the current [[io.flatmap.ml.som.SelfOrganizingMap]] instance and actual [[io.flatmap.ml.som.SelfOrganizingMap.Parameters]]
    */
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
