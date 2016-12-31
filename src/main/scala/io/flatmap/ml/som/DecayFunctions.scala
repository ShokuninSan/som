package io.flatmap.ml.som

trait DecayFunction {

  def decay(coefficient: Double, iteration: Int, maxIterations: Int): Double

}

/** Custom decay
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
trait CustomDecay extends DecayFunction { self: SelfOrganizingMap =>

  /** Computes the decay for coefficients
    *
    * This method decreases coefficients like sigma and learningRate up to one half of its initial value.
    *
    * @param coefficient the coefficient for decay
    * @param iteration the current iteration
    * @param maxIterations the maximum amount of iterations/epochs
    * @return decayed coefficient
    */
  override def decay(coefficient: Double, iteration: Int, maxIterations: Int): Double = {
    coefficient/(1.0+iteration.toDouble/maxIterations.toDouble)
  }

}
