package io.flatmap.ml.som

import io.flatmap.ml.som.SelfOrganizingMap.Shape

object GaussianSelfOrganizingMap {

  def apply(width: Int, height: Int, _sigma: Double = 0.2, _learningRate: Double = 0.1) =
    new SelfOrganizingMap with GaussianNeighborboodKernel with CustomDecay with QuantizationErrorMetrics {
      override val shape: Shape = (width, height)
      override val sigma: Double = _sigma
      override val learningRate: Double = _learningRate
    }

}
