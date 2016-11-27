package io.flatmap.ml.som

import breeze.linalg.DenseMatrix
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.random.RandomRDDs
import org.scalatest._
import util.TestSparkContext

class SelfOrganizingMapSpec extends FlatSpec with Matchers with BeforeAndAfterEach with TestSparkContext {

  "instantiation" should "create a SOM with codebook of zeros" in {
    val som = new SelfOrganizingMap(6, 6)
    som.codebook should === (DenseMatrix.fill[Array[Double]](6, 6)(Array.emptyDoubleArray))
  }

  "initialize" should "copy random data points from RDD into codebook" in {
    val data = RandomRDDs.normalVectorRDD(sparkSession.sparkContext, numRows = 512L, numCols = 3)
    val som = new SelfOrganizingMap(6, 6)
    som.initialize(data)
    som.codebook should !== (DenseMatrix.fill[Array[Double]](6, 6)(Array.emptyDoubleArray))
  }

  "winner" should "return best matching unit (BMU)" in {
    val som = new SelfOrganizingMap(6, 6)
    som.codebook.keysIterator.foreach { case (x, y) => som.codebook(x, y) = Array(0.2, 0.2, 0.2) }
    som.codebook(3, 3) = Array(0.3, 0.3, 0.3)
    som.winner(new DenseVector(Array(2.0, 2.0, 2.0))) should equal ((3, 3))
    som.winner(new DenseVector(Array(0.26, 0.26, 0.26))) should equal ((3, 3))
  }

  "winner" should "return last best matching unit (BMU) index in case of multiple BMUs" in {
    val som = new SelfOrganizingMap(6, 6)
    som.codebook.keysIterator.foreach { case (x, y) => som.codebook(x, y) = Array(0.2, 0.2, 0.2) }
    som.codebook(3, 3) = Array(0.3, 0.3, 0.3)
    som.winner(new DenseVector(Array(0.25, 0.25, 0.25))) should equal ((5, 5))
  }

  "gaussian" should "return a neighborhood matrix with gaussian distributed coefficients" in {
    val som = new SelfOrganizingMap(7, 7)
    som.codebook.foreachKey {
      case (i, j) =>
        som.codebook(i, j) = breeze.linalg.DenseVector.ones[Double](7).toArray
    }
    val gNeighborhood = som.gaussian((3, 3))
    gNeighborhood(3, 3) should equal (1.0)
    gNeighborhood(6, 6) should equal (gNeighborhood(0, 0))
    gNeighborhood(0, 6) should equal (gNeighborhood(0, 0))
    gNeighborhood(6, 0) should equal (gNeighborhood(0, 0))
  }

}
