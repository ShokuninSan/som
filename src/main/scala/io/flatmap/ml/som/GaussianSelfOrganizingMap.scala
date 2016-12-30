package io.flatmap.ml.som

import breeze.linalg.DenseMatrix
import io.flatmap.ml.som.SelfOrganizingMap.CodeBook

class GaussianSelfOrganizingMap private (var codeBook: CodeBook, val sigma: Double, val learningRate: Double)
  extends SelfOrganizingMap
    with GaussianNeighborboodKernel
    with CustomDecay
    with QuantizationErrorMetrics

object GaussianSelfOrganizingMap {

  def apply(codeBook: CodeBook, sigma: Double, learningRate: Double) = {
    new GaussianSelfOrganizingMap(codeBook, sigma, learningRate)
  }

  def apply(width: Int, height: Int, sigma: Double = 0.2, learningRate: Double = 0.1) = {
    new GaussianSelfOrganizingMap(DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray), sigma, learningRate)
  }

}
