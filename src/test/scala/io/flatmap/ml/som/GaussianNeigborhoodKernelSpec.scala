package io.flatmap.ml.som

import io.flatmap.ml.som.SelfOrganizingMap.{Parameters, Shape}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import util.{FakeDecayFunction, FakeMetrics, TestSparkContext}

class GaussianNeigborhoodKernelSpec extends FlatSpec with Matchers with BeforeAndAfterEach with TestSparkContext {

  def SOM(width: Int, height: Int) =
    new SelfOrganizingMap with GaussianNeighborboodKernel with FakeDecayFunction with FakeMetrics {
      override val shape: Shape = (width, height)
      override val learningRate: Double = 0.1
      override val sigma: Double = 0.2
    }

  "neighborhood" should "return a matrix with gaussian distributed coefficients" in {
    val som = SOM(7, 7)
    som.codeBook.foreachKey {
      case (i, j) =>
        som.codeBook(i, j) = breeze.linalg.DenseVector.ones[Double](7).toArray
    }
    implicit val hp = Parameters(sigma = som.sigma, learningRate = som.learningRate)
    val gNeighborhood = som.neighborhood((3, 3))
    gNeighborhood(3, 3) should equal (1.0)
    gNeighborhood(6, 6) should equal (gNeighborhood(0, 0))
    gNeighborhood(0, 6) should equal (gNeighborhood(0, 0))
    gNeighborhood(6, 0) should equal (gNeighborhood(0, 0))
  }

}
