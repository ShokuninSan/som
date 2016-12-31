package io.flatmap.ml.som

object GaussianSelfOrganizingMap {

  def apply(_width: Int, _height: Int, _sigma: Double = 0.2, _learningRate: Double = 0.1) = {
    new SelfOrganizingMap
      with GaussianNeighborboodKernel
      with CustomDecay
      with QuantizationErrorMetrics {
      override val width: Int = _width
      override val height: Int = _height
      override val sigma: Double = _sigma
      override val learningRate: Double = _learningRate
    }
  }

}
