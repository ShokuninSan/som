package io.flatmap.ml.som

import org.apache.spark.mllib.random.RandomRDDs
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import util.TestSparkContext

class CustomDecaySpec extends FlatSpec with Matchers with BeforeAndAfterEach with TestSparkContext {

  def SOM(_width: Int, _height: Int, _sigma: Double, _learningRate: Double) =
    new SelfOrganizingMap with CustomDecay with GaussianNeighborboodKernel with QuantizationErrorMetrics {
      override val width: Int = _width
      override val height: Int = _height
      override val learningRate: Double = _learningRate
      override val sigma: Double = _sigma
    }

  "decay" should "decrease sigma and learningRate to one half each" in {
    val data = RandomRDDs.normalVectorRDD(sparkSession.sparkContext, numRows = 512L, numCols = 3)
    val sigma = 0.5
    val learningRate = 0.3
    val iterations = 20.0
    val (_, params) =
      SOM(6, 6, sigma, learningRate)
        .initialize(data)
        .train(data, iterations.toInt)
    params.sigma should equal (sigma / (1.0 + (iterations - 1.0) / iterations))
    params.learningRate should equal (learningRate / (1.0 + (iterations - 1.0) / iterations))
  }

}
