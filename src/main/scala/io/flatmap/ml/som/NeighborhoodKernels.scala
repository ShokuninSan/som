package io.flatmap.ml.som

import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.numerics._
import io.flatmap.ml.numerics._
import io.flatmap.ml.som.SelfOrganizingMap.{Parameters, Neuron}

trait NeighborhoodKernel {

  def neighborhood(winner: Neuron)(implicit p: Parameters): DenseMatrix[Double]

}

/** A Gaussian Neigborhood Kernel
  *
  * This trait is for composition with a [[io.flatmap.ml.som.SelfOrganizingMap]]:
  *
  * {{{
  * val SOM = new SelfOrganizingMap with CustomDecay with GaussianNeighborboodKernel with QuantizationErrorMetrics {
  *   override val shape: Shape = (24, 24)
  *   override val learningRate: Double = 0.3
  *   override val sigma: Double = 0.5
  * }
  * }}}
  */
trait GaussianNeighborboodKernel extends NeighborhoodKernel { self: SelfOrganizingMap =>

  /** Creates a vector with Gaussian distributed values around mean index
    *
    * @param size length of the vector
    * @param mean index of the vector which represents the mean of the distribution
    * @param sigma the sigma of the distribution
    * @return a [[breeze.linalg.DenseVector]] with Gaussian distributed values around mean index
    */
  def gaussian(size: Int, mean: Int, sigma: Double): DenseVector[Double] = {
    val initDistributions = DenseVector.range(0, size) - mean
    val numerator = pow(initDistributions, 2).map(_.toDouble)
    val denominator = 2*math.Pi*sigma*sigma
    exp(-(numerator :/ denominator))
  }

  /** Creates a matrix with Gaussian distributed values around the Best Matching Unit
    *
    * @param winner the Best Matching Unit (BMU) of the SOM
    * @param p a [[io.flatmap.ml.som.SelfOrganizingMap.Parameters]] instance with actual learning rate
    * @return a [[breeze.linalg.DenseMatrix]] with Gaussian distributed values around the Best Matching Unit
    */
  def neighborhood(winner: Neuron)(implicit p: Parameters): DenseMatrix[Double] = {
    // vectors with gaussian distribution of neighborhood coefficients
    val gaussianX = gaussian(codeBook.cols, winner._1, p.sigma)
    val gaussianY = gaussian(codeBook.rows, winner._2, p.sigma)
    // return 2 dimensional gaussian dist. surface by creating the outer product
    outer(gaussianX, gaussianY)
  }

}